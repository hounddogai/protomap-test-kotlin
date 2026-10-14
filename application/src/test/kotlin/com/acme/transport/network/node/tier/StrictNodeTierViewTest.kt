package com.acme.transport.network.node.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictNodeTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictNodeTier("code-949", "title-280")
        val view = toStrictNodeTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictNodeTierViews(mutableListOf<StrictNodeTier>(item, item)).size, "views")
    }
}
