package com.acme.transport.lastmile.attempt.attemptdigest

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedAttemptDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707418378)
        val items = mutableListOf<WeightedAttemptDigest>()
        for (index in 0 until 2) {
            items.add(newWeightedAttemptDigest("weighted-attempt-digest-$index", "sample"))
        }
        val report = buildWeightedAttemptDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
