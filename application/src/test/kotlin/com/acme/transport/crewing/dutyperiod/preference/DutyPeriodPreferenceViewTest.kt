package com.acme.transport.crewing.dutyperiod.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodPreference("code-132", "title-328")
        val view = toDutyPeriodPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodPreferenceViews(mutableListOf<DutyPeriodPreference>(item, item)).size, "views")
    }
}
