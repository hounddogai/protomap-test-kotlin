package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedParcelPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702705838)
        val items = mutableListOf<WeightedParcelPolicy>()
        for (index in 0 until 2) {
            items.add(newWeightedParcelPolicy("weighted-parcel-policy-$index", "sample"))
        }
        val report = buildWeightedParcelPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
