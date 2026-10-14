package com.acme.transport.crewing.swap.swapclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimarySwapMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimarySwapMapping("code-382", "title-295")
        val view = toPrimarySwapMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimarySwapMappingViews(mutableListOf<PrimarySwapMapping>(item, item)).size, "views")
    }
}
