package com.acme.transport.customs.hold.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientHoldGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientHoldGroup("code-421", "label-32")
        val view = toLenientHoldGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientHoldGroupViews(mutableListOf<LenientHoldGroup>(item, item)).size, "views")
    }
}
