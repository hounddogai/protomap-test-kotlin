package com.acme.transport.crewing.restbreak.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class RestBreakTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRestBreakTier("code-727", "name-693")
        val view = toRestBreakTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRestBreakTierViews(mutableListOf<RestBreakTier>(item, item)).size, "views")
    }
}
