package com.acme.transport.crewing.swap.group

import kotlin.test.Test
import kotlin.test.assertEquals

class SwapGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSwapGroup("code-56", "title-695")
        val view = toSwapGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSwapGroupViews(mutableListOf<SwapGroup>(item, item)).size, "views")
    }
}
