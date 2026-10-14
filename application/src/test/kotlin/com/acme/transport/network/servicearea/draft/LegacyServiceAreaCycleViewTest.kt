package com.acme.transport.network.servicearea.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyServiceAreaCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyServiceAreaCycle("code-925", "title-326")
        val view = toLegacyServiceAreaCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyServiceAreaCycleViews(mutableListOf<LegacyServiceAreaCycle>(item, item)).size, "views")
    }
}
