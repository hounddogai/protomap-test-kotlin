package com.acme.transport.crewing.dutyperiod.periodthreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualDutyPeriodExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualDutyPeriodException("code-710", "title-180")
        val view = toManualDutyPeriodExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualDutyPeriodExceptionViews(mutableListOf<ManualDutyPeriodException>(item, item)).size, "views")
    }
}
