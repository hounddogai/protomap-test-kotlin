package com.acme.transport.emissions.idling.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingEstimate("code-672", "name-656")
        val view = toIdlingEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingEstimateViews(mutableListOf<IdlingEstimate>(item, item)).size, "views")
    }
}
