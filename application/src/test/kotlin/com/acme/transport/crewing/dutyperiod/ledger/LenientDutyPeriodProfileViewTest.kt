package com.acme.transport.crewing.dutyperiod.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientDutyPeriodProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientDutyPeriodProfile("code-500", "name-862")
        val view = toLenientDutyPeriodProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientDutyPeriodProfileViews(mutableListOf<LenientDutyPeriodProfile>(item, item)).size, "views")
    }
}
