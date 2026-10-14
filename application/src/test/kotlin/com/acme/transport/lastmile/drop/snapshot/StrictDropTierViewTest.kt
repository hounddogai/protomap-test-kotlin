package com.acme.transport.lastmile.drop.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictDropTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictDropTier("code-305", "title-828")
        val view = toStrictDropTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictDropTierViews(mutableListOf<StrictDropTier>(item, item)).size, "views")
    }
}
