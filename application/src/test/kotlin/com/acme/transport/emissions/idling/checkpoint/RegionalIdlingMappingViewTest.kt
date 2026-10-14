package com.acme.transport.emissions.idling.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalIdlingMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalIdlingMapping("code-341", "title-107")
        val view = toRegionalIdlingMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalIdlingMappingViews(mutableListOf<RegionalIdlingMapping>(item, item)).size, "views")
    }
}
