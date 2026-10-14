package com.acme.transport.lastmile.locker.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultLockerPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultLockerPlan("code-574", "title-711")
        val view = toDefaultLockerPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultLockerPlanViews(mutableListOf<DefaultLockerPlan>(item, item)).size, "views")
    }
}
