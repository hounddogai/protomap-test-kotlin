package com.acme.transport.lastmile.drop.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class DropProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropProfile("code-614", "label-632")
        val view = toDropProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropProfileViews(mutableListOf<DropProfile>(item, item)).size, "views")
    }
}
