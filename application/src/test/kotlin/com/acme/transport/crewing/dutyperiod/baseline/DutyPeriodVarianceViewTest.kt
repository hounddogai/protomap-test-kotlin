package com.acme.transport.crewing.dutyperiod.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class DutyPeriodVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDutyPeriodVariance("code-599", "name-403")
        val view = toDutyPeriodVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDutyPeriodVarianceViews(mutableListOf<DutyPeriodVariance>(item, item)).size, "views")
    }
}
