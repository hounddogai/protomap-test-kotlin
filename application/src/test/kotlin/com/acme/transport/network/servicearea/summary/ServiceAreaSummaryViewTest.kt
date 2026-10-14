package com.acme.transport.network.servicearea.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaSummary("code-784", "name-989")
        val view = toServiceAreaSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaSummaryViews(mutableListOf<ServiceAreaSummary>(item, item)).size, "views")
    }
}
