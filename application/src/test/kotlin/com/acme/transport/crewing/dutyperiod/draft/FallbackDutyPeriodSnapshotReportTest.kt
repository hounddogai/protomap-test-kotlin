package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDutyPeriodSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709133853)
        val items = mutableListOf<FallbackDutyPeriodSnapshot>()
        for (index in 0 until 5) {
            items.add(newFallbackDutyPeriodSnapshot("fallback-duty-period-snapshot-$index", "sample"))
        }
        val report = buildFallbackDutyPeriodSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
