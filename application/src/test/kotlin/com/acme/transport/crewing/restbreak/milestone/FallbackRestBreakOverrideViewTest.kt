package com.acme.transport.crewing.restbreak.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackRestBreakOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackRestBreakOverride("code-275", "label-722")
        val view = toFallbackRestBreakOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackRestBreakOverrideViews(mutableListOf<FallbackRestBreakOverride>(item, item)).size, "views")
    }
}
