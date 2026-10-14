package com.acme.transport.terminal.traileryard.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardEstimate("code-862", "label-903")
        val view = toTrailerYardEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardEstimateViews(mutableListOf<TrailerYardEstimate>(item, item)).size, "views")
    }
}
