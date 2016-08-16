package pl.allegro.tech.hermes.consumers.consumer.offset

import com.codahale.metrics.MetricRegistry
import pl.allegro.tech.hermes.api.SubscriptionName
import pl.allegro.tech.hermes.common.config.ConfigFactory
import pl.allegro.tech.hermes.common.metric.HermesMetrics
import pl.allegro.tech.hermes.metrics.PathsCompiler
import spock.lang.Specification

class OffsetCommitterTest extends Specification {

    private OffsetQueue queue = new OffsetQueue(
            new HermesMetrics(new MetricRegistry(), new PathsCompiler("host")),
            new ConfigFactory()
    )

    private MockMessageCommitter messageCommitter = new MockMessageCommitter()

    private OffsetCommitter committer = new OffsetCommitter(
            queue, [messageCommitter], new HermesMetrics(new MetricRegistry(), new PathsCompiler("host"))
    )

    def "should commit smallest offset of uncommitted message"() {
        given:
        committer.offerInflightOffset(offset(1, 1))
        committer.offerInflightOffset(offset(1, 2))
        committer.offerInflightOffset(offset(1, 3))
        committer.offerInflightOffset(offset(1, 4))

        committer.offerCommittedOffset(offset(1, 1))
        committer.offerCommittedOffset(offset(1, 4))

        when:
        committer.commit()

        then:
        messageCommitter.wereCommitted(1, offset(1, 2))
    }

    def "should increment offset by 1 only if it comes from committed offsets to match Kafka offset definition"() {
        given:
        committer.offerInflightOffset(offset(1, 1))
        committer.offerCommittedOffset(offset(1, 1))

        queue.offerInflightOffset(offset(2, 1))

        when:
        committer.commit()

        then:
        messageCommitter.wereCommitted(1, offset(1, 2), offset(2, 1))
    }

    def "should commit max offset of committed offsets when no smaller inflights exist"() {
        given:
        committer.offerInflightOffset(offset(1, 3))
        committer.offerInflightOffset(offset(1, 4))

        committer.offerCommittedOffset(offset(1, 3))
        committer.offerCommittedOffset(offset(1, 4))

        when:
        committer.commit()

        then:
        messageCommitter.wereCommitted(1, offset(1, 5))
    }

    def "should commit same offset twice when there are no new offsets to commit"() {
        given:
        committer.offerInflightOffset(offset(1, 5))

        when:
        committer.commit()

        then:
        messageCommitter.wereCommitted(1, offset(1, 5))

        when:
        committer.commit()

        then:
        messageCommitter.wereCommitted(2, offset(1, 5))
    }

    def "should not mix offsets from different partitions and topics"() {
        given:
        committer.offerInflightOffset(offset(1, 3))
        committer.offerInflightOffset(offset(1, 4))

        committer.offerInflightOffset(offset(2, 10))
        committer.offerInflightOffset(offset(2, 11))

        committer.offerCommittedOffset(offset(1, 3))
        committer.offerCommittedOffset(offset(1, 4))
        committer.offerCommittedOffset(offset(2, 11))

        when:
        committer.commit()

        then:
        messageCommitter.wereCommitted(1, offset(1, 5), offset(2, 10))
    }

    def "should get rid of leftover inflight offset commits on second iteration when removing subscription"() {
        given:
        committer.offerInflightOffset(offset(1, 3))

        when:
        committer.removeUncommittedOffsets(SubscriptionName.fromString('group.topic$sub'))
        committer.commit()

        then:
        messageCommitter.wereCommitted(1, offset(1, 3))

        when:
        committer.commit()

        then:
        messageCommitter.wereCommitted(2)
    }

    def "should retry committing offsets that failed to commit on first try in next iteration"() {
        given:
        committer.offerInflightOffset(offset(1, 1))
        committer.offerCommittedOffset(offset(1, 1))

        FailedToCommitOffsets failedResult = new FailedToCommitOffsets()
        failedResult.add(offset(1, 2))

        messageCommitter.returnValue(failedResult)

        when:
        committer.commit()
        committer.commit()

        then:
        messageCommitter.wereCommitted(1, offset(1, 2))
        messageCommitter.wereCommitted(2, offset(1, 2))
    }

    private SubscriptionPartitionOffset offset(int partition, long offset) {
        return SubscriptionPartitionOffset.subscriptionPartitionOffset("group_topic", 'group.topic$sub', partition, offset)
    }
}
