package com.acme.transport.linehaul.laneplan.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalLanePlanItemViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalLanePlanItem("code-615", "title-478")
        val view = toGlobalLanePlanItemView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalLanePlanItemViews(mutableListOf<GlobalLanePlanItem>(item, item)).size, "views")
    }
}
