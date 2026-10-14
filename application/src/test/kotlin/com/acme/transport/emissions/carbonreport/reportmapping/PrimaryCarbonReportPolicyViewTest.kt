package com.acme.transport.emissions.carbonreport.reportmapping

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryCarbonReportPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryCarbonReportPolicy("code-119", "name-867")
        val view = toPrimaryCarbonReportPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryCarbonReportPolicyViews(mutableListOf<PrimaryCarbonReportPolicy>(item, item)).size, "views")
    }
}
