package com.acme.transport.lastmile.locker.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class LockerMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLockerMapping("code-565", "label-298")
        val view = toLockerMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLockerMappingViews(mutableListOf<LockerMapping>(item, item)).size, "views")
    }
}
