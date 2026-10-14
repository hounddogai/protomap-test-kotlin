package com.acme.transport.crewing.swap.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class SwapDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSwapDraft("code-269", "name-757")
        val view = toSwapDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSwapDraftViews(mutableListOf<SwapDraft>(item, item)).size, "views")
    }
}
