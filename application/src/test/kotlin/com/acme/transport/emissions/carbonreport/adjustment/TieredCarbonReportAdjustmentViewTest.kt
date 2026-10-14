package com.acme.transport.emissions.carbonreport.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredCarbonReportAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredCarbonReportAdjustment("code-867", "label-576")
        val view = toTieredCarbonReportAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredCarbonReportAdjustmentViews(mutableListOf<TieredCarbonReportAdjustment>(item, item)).size, "views")
    }
}
