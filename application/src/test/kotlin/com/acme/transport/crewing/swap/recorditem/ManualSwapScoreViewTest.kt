package com.acme.transport.crewing.swap.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualSwapScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualSwapScore("code-965", "title-396")
        val view = toManualSwapScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualSwapScoreViews(mutableListOf<ManualSwapScore>(item, item)).size, "views")
    }
}
