package com.acme.transport.customs.hold.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedHoldRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedHoldRollup("code-69", "title-449")
        val view = toCappedHoldRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedHoldRollupViews(mutableListOf<CappedHoldRollup>(item, item)).size, "views")
    }
}
