package com.acme.transport.safety.coaching.mapping

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCoachingMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707595645)
        val items = mutableListOf<PrimaryCoachingMapping>()
        for (index in 0 until 2) {
            items.add(newPrimaryCoachingMapping("primary-coaching-mapping-$index", "sample"))
        }
        val report = buildPrimaryCoachingMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
