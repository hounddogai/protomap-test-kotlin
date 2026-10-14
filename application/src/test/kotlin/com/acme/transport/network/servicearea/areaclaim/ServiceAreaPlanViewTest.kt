package com.acme.transport.network.servicearea.areaclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaPlan("code-52", "title-246")
        val view = toServiceAreaPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaPlanViews(mutableListOf<ServiceAreaPlan>(item, item)).size, "views")
    }
}
