package com.acme.transport.linehaul.tender.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderSchedule("code-533", "label-924")
        val view = toTenderScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderScheduleViews(mutableListOf<TenderSchedule>(item, item)).size, "views")
    }
}
