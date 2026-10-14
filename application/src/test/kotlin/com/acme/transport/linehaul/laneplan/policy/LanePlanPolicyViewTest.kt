package com.acme.transport.linehaul.laneplan.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class LanePlanPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLanePlanPolicy("code-566", "name-921")
        val view = toLanePlanPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLanePlanPolicyViews(mutableListOf<LanePlanPolicy>(item, item)).size, "views")
    }
}
