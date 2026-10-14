package com.acme.transport.crewing.swap.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalSwapOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalSwapOverride("code-345", "label-967")
        val view = toGlobalSwapOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalSwapOverrideViews(mutableListOf<GlobalSwapOverride>(item, item)).size, "views")
    }
}
