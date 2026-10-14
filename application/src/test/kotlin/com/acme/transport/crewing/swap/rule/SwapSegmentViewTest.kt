package com.acme.transport.crewing.swap.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class SwapSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSwapSegment("code-148", "title-231")
        val view = toSwapSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSwapSegmentViews(mutableListOf<SwapSegment>(item, item)).size, "views")
    }
}
