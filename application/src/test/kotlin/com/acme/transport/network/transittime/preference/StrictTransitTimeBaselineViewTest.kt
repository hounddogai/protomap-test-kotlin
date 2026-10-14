package com.acme.transport.network.transittime.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictTransitTimeBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictTransitTimeBaseline("code-251", "label-943")
        val view = toStrictTransitTimeBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictTransitTimeBaselineViews(mutableListOf<StrictTransitTimeBaseline>(item, item)).size, "views")
    }
}
