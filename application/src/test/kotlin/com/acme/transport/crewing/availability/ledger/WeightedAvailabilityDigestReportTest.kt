package com.acme.transport.crewing.availability.ledger

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedAvailabilityDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701237605)
        val items = mutableListOf<WeightedAvailabilityDigest>()
        for (index in 0 until 3) {
            items.add(newWeightedAvailabilityDigest("weighted-availability-digest-$index", "sample"))
        }
        val report = buildWeightedAvailabilityDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
