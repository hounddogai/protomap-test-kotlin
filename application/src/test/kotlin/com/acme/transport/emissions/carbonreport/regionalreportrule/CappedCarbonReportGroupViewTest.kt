package com.acme.transport.emissions.carbonreport.regionalreportrule

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedCarbonReportGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedCarbonReportGroup("code-517", "name-190")
        val view = toCappedCarbonReportGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedCarbonReportGroupViews(mutableListOf<CappedCarbonReportGroup>(item, item)).size, "views")
    }
}
