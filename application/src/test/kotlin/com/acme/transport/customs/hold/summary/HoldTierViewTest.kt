package com.acme.transport.customs.hold.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHoldTier("code-88", "title-654")
        val view = toHoldTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHoldTierViews(mutableListOf<HoldTier>(item, item)).size, "views")
    }
}
