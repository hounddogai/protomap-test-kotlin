package com.acme.transport.lastmile.locker.group

import kotlin.test.Test
import kotlin.test.assertEquals

class LockerBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLockerBaseline("code-272", "title-178")
        val view = toLockerBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLockerBaselineViews(mutableListOf<LockerBaseline>(item, item)).size, "views")
    }
}
