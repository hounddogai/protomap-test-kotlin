package com.acme.transport.crewing.swap.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveSwapWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveSwapWindow("code-792", "label-664")
        val view = toAdaptiveSwapWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveSwapWindowViews(mutableListOf<AdaptiveSwapWindow>(item, item)).size, "views")
    }
}
