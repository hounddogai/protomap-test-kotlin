package com.acme.transport.lastmile.locker.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingLockerPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingLockerPlan("code-497", "title-766")
        val view = toRollingLockerPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingLockerPlanViews(mutableListOf<RollingLockerPlan>(item, item)).size, "views")
    }
}
