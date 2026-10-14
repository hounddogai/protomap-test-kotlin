package com.acme.transport.crewing.dutyperiod.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedDutyPeriodForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedDutyPeriodForecast("code-679", "name-882")
        val view = toBatchedDutyPeriodForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedDutyPeriodForecastViews(mutableListOf<BatchedDutyPeriodForecast>(item, item)).size, "views")
    }
}
