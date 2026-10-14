package com.acme.transport.network.transittime.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeVariance("code-753", "title-789")
        val view = toTransitTimeVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeVarianceViews(mutableListOf<TransitTimeVariance>(item, item)).size, "views")
    }
}
