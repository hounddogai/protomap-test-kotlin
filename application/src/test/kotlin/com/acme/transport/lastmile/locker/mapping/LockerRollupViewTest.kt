package com.acme.transport.lastmile.locker.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class LockerRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLockerRollup("code-507", "name-475")
        val view = toLockerRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLockerRollupViews(mutableListOf<LockerRollup>(item, item)).size, "views")
    }
}
