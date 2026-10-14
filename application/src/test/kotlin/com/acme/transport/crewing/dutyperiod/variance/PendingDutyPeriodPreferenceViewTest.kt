package com.acme.transport.crewing.dutyperiod.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingDutyPeriodPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingDutyPeriodPreference("code-20", "label-162")
        val view = toPendingDutyPeriodPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingDutyPeriodPreferenceViews(mutableListOf<PendingDutyPeriodPreference>(item, item)).size, "views")
    }
}
