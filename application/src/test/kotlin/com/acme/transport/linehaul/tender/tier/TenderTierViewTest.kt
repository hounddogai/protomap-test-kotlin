package com.acme.transport.linehaul.tender.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderTier("code-127", "name-847")
        val view = toTenderTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderTierViews(mutableListOf<TenderTier>(item, item)).size, "views")
    }
}
