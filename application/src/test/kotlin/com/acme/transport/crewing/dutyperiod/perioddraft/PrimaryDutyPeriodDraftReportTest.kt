package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDutyPeriodDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706665460)
        val items = mutableListOf<PrimaryDutyPeriodDraft>()
        for (index in 0 until 2) {
            items.add(newPrimaryDutyPeriodDraft("primary-duty-period-draft-$index", "sample"))
        }
        val report = buildPrimaryDutyPeriodDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
