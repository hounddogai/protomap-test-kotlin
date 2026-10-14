package com.acme.transport.customs.hold.group

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHoldGroup("code-765", "name-675")
        val view = toHoldGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHoldGroupViews(mutableListOf<HoldGroup>(item, item)).size, "views")
    }
}
