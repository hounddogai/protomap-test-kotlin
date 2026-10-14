package com.acme.transport.emissions.carbonreport.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientCarbonReportVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientCarbonReportVariance("code-404", "label-101")
        val view = toLenientCarbonReportVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientCarbonReportVarianceViews(mutableListOf<LenientCarbonReportVariance>(item, item)).size, "views")
    }
}
