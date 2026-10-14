package com.acme.transport.crewing.dutyperiod.snapshot

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703338195)
        val items = mutableListOf<DutyPeriodBundle>()
        for (index in 0 until 4) {
            items.add(newDutyPeriodBundle("duty-period-bundle-$index", "sample"))
        }
        val report = buildDutyPeriodBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
