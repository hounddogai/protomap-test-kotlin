package com.acme.transport.crewing.swap.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class SwapVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSwapVariance("code-962", "title-775")
        val view = toSwapVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSwapVarianceViews(mutableListOf<SwapVariance>(item, item)).size, "views")
    }
}
