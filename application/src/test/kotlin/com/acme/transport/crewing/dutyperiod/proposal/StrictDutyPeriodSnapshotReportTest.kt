package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDutyPeriodSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708290051)
        val items = mutableListOf<StrictDutyPeriodSnapshot>()
        for (index in 0 until 5) {
            items.add(newStrictDutyPeriodSnapshot("strict-duty-period-snapshot-$index", "sample"))
        }
        val report = buildStrictDutyPeriodSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
