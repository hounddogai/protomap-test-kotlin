package com.acme.transport.crewing.restbreak.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveRestBreakExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveRestBreakException("code-149", "label-422")
        val view = toAdaptiveRestBreakExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveRestBreakExceptionViews(mutableListOf<AdaptiveRestBreakException>(item, item)).size, "views")
    }
}
