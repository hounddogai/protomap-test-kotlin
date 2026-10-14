package com.acme.transport.crewing.dutyperiod.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultDutyPeriodEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultDutyPeriodEntry("code-726", "label-966")
        val view = toDefaultDutyPeriodEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultDutyPeriodEntryViews(mutableListOf<DefaultDutyPeriodEntry>(item, item)).size, "views")
    }
}
