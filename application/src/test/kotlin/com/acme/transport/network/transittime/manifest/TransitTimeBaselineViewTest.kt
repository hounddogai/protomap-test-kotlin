package com.acme.transport.network.transittime.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeBaseline("code-554", "name-336")
        val view = toTransitTimeBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeBaselineViews(mutableListOf<TransitTimeBaseline>(item, item)).size, "views")
    }
}
