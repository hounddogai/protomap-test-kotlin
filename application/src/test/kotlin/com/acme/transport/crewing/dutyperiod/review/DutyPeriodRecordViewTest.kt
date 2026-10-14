package com.acme.transport.crewing.dutyperiod.review

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodRecord("code-704", "name-204")
        val view = toDutyPeriodRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodRecordViews(mutableListOf<DutyPeriodRecord>(item, item)).size, "views")
    }
}
