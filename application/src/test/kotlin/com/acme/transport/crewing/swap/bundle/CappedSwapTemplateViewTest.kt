package com.acme.transport.crewing.swap.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedSwapTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedSwapTemplate("code-498", "name-345")
        val view = toCappedSwapTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedSwapTemplateViews(mutableListOf<CappedSwapTemplate>(item, item)).size, "views")
    }
}
