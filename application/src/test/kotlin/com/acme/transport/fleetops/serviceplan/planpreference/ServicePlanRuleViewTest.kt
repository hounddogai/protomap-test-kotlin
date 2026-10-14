package com.acme.transport.fleetops.serviceplan.planpreference

import kotlin.test.Test
import kotlin.test.assertEquals

class ServicePlanRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServicePlanRule("code-41", "label-731")
        val view = toServicePlanRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServicePlanRuleViews(mutableListOf<ServicePlanRule>(item, item)).size, "views")
    }
}
