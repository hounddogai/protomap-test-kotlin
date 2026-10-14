package com.acme.transport.lastmile.locker.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class LockerPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLockerPolicy("code-77", "title-108")
        val view = toLockerPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLockerPolicyViews(mutableListOf<LockerPolicy>(item, item)).size, "views")
    }
}
