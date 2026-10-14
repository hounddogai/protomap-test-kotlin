package com.acme.transport.crewing.qualification.qualificationmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingQualificationForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingQualificationForecast("code-997", "title-646")
        val view = toPendingQualificationForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingQualificationForecastViews(mutableListOf<PendingQualificationForecast>(item, item)).size, "views")
    }
}
