package com.acme.transport.crewing.swap.swapschedule

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackSwapPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackSwapPolicy("code-82", "label-886")
        val view = toFallbackSwapPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackSwapPolicyViews(mutableListOf<FallbackSwapPolicy>(item, item)).size, "views")
    }
}
