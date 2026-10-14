package com.acme.transport.safety.nearmiss.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class NearMissPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNearMissPlan("code-764", "label-89")
        val view = toNearMissPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNearMissPlanViews(mutableListOf<NearMissPlan>(item, item)).size, "views")
    }
}
