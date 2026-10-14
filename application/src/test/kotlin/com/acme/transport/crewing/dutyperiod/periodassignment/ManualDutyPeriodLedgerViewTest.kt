package com.acme.transport.crewing.dutyperiod.periodassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualDutyPeriodLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualDutyPeriodLedger("code-696", "label-975")
        val view = toManualDutyPeriodLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualDutyPeriodLedgerViews(mutableListOf<ManualDutyPeriodLedger>(item, item)).size, "views")
    }
}
