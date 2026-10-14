package com.acme.transport.lastmile.locker.lockerprofile

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackLockerPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackLockerPlan("code-724", "title-742")
        val view = toFallbackLockerPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackLockerPlanViews(mutableListOf<FallbackLockerPlan>(item, item)).size, "views")
    }
}
