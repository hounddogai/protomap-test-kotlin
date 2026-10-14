package com.acme.transport.lastmile.locker.lockerbudget

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultLockerSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultLockerSlot("code-362", "name-365")
        val view = toDefaultLockerSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultLockerSlotViews(mutableListOf<DefaultLockerSlot>(item, item)).size, "views")
    }
}
