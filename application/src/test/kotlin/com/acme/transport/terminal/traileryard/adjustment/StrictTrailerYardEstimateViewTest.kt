package com.acme.transport.terminal.traileryard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictTrailerYardEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictTrailerYardEstimate("code-642", "name-354")
        val view = toStrictTrailerYardEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictTrailerYardEstimateViews(mutableListOf<StrictTrailerYardEstimate>(item, item)).size, "views")
    }
}
