package com.acme.transport.crewing.dutyperiod.periodnotice

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualDutyPeriodNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualDutyPeriodNotice("code-147", "label-252")
        val view = toManualDutyPeriodNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualDutyPeriodNoticeViews(mutableListOf<ManualDutyPeriodNotice>(item, item)).size, "views")
    }
}
