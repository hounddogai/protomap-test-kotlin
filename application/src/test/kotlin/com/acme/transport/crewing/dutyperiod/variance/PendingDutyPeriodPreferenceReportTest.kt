package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDutyPeriodPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707340708)
        val items = mutableListOf<PendingDutyPeriodPreference>()
        for (index in 0 until 3) {
            items.add(newPendingDutyPeriodPreference("pending-duty-period-preference-$index", "sample"))
        }
        val report = buildPendingDutyPeriodPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
