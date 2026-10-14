package com.acme.transport.crewing.swap.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class SwapTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSwapTarget("code-650", "name-971")
        val view = toSwapTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSwapTargetViews(mutableListOf<SwapTarget>(item, item)).size, "views")
    }
}
