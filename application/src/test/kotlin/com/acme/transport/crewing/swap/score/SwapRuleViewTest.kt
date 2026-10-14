package com.acme.transport.crewing.swap.score

import kotlin.test.Test
import kotlin.test.assertEquals

class SwapRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSwapRule("code-831", "title-181")
        val view = toSwapRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSwapRuleViews(mutableListOf<SwapRule>(item, item)).size, "views")
    }
}
