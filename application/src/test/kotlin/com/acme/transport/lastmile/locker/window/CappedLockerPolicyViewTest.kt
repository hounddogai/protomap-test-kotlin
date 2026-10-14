package com.acme.transport.lastmile.locker.window

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedLockerPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedLockerPolicy("code-742", "name-480")
        val view = toCappedLockerPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedLockerPolicyViews(mutableListOf<CappedLockerPolicy>(item, item)).size, "views")
    }
}
