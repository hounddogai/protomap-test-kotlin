package com.acme.transport.emissions.idling.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingMapping("code-219", "title-128")
        val view = toIdlingMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingMappingViews(mutableListOf<IdlingMapping>(item, item)).size, "views")
    }
}
