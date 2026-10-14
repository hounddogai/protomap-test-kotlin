package com.acme.transport.linehaul.laneplan.window

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyLanePlanBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyLanePlanBundle("code-73", "title-344")
        val view = toLegacyLanePlanBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyLanePlanBundleViews(mutableListOf<LegacyLanePlanBundle>(item, item)).size, "views")
    }
}
