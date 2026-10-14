package com.acme.transport.crewing.dutyperiod.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodException("code-179", "label-516")
        val view = toDutyPeriodExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodExceptionViews(mutableListOf<DutyPeriodException>(item, item)).size, "views")
    }
}
