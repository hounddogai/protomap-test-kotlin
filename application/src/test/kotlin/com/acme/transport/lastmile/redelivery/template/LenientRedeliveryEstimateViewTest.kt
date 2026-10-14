package com.acme.transport.lastmile.redelivery.template

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientRedeliveryEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientRedeliveryEstimate("code-115", "title-954")
        val view = toLenientRedeliveryEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientRedeliveryEstimateViews(mutableListOf<LenientRedeliveryEstimate>(item, item)).size, "views")
    }
}
