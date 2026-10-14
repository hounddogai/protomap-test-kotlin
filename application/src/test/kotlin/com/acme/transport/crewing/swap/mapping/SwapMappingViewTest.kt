package com.acme.transport.crewing.swap.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class SwapMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSwapMapping("code-188", "label-923")
        val view = toSwapMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSwapMappingViews(mutableListOf<SwapMapping>(item, item)).size, "views")
    }
}
