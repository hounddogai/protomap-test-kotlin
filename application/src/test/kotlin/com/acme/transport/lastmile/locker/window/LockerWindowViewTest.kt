package com.acme.transport.lastmile.locker.window

import kotlin.test.Test
import kotlin.test.assertEquals

class LockerWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLockerWindow("code-283", "name-692")
        val view = toLockerWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLockerWindowViews(mutableListOf<LockerWindow>(item, item)).size, "views")
    }
}
