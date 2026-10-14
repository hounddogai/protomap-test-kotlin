package com.acme.transport.linehaul.trailer.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveTrailerRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveTrailerRequest("code-915", "name-803")
        val view = toAdaptiveTrailerRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveTrailerRequestViews(mutableListOf<AdaptiveTrailerRequest>(item, item)).size, "views")
    }
}
