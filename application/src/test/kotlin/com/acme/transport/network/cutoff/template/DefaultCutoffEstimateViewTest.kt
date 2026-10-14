package com.acme.transport.network.cutoff.template

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultCutoffEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultCutoffEstimate("code-656", "title-881")
        val view = toDefaultCutoffEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultCutoffEstimateViews(mutableListOf<DefaultCutoffEstimate>(item, item)).size, "views")
    }
}
