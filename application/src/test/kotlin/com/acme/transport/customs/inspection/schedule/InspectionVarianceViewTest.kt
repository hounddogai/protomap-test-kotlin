package com.acme.transport.customs.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionVariance("code-559", "name-902")
        val view = toInspectionVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionVarianceViews(mutableListOf<InspectionVariance>(item, item)).size, "views")
    }
}
