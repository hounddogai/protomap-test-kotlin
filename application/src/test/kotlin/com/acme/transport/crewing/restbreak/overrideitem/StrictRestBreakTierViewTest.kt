package com.acme.transport.crewing.restbreak.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictRestBreakTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictRestBreakTier("code-859", "title-142")
        val view = toStrictRestBreakTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictRestBreakTierViews(mutableListOf<StrictRestBreakTier>(item, item)).size, "views")
    }
}
