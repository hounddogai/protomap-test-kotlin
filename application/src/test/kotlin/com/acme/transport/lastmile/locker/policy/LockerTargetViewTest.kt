package com.acme.transport.lastmile.locker.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class LockerTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLockerTarget("code-125", "name-130")
        val view = toLockerTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLockerTargetViews(mutableListOf<LockerTarget>(item, item)).size, "views")
    }
}
