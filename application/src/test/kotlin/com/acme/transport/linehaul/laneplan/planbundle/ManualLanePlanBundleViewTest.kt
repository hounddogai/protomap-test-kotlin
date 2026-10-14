package com.acme.transport.linehaul.laneplan.planbundle

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualLanePlanBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualLanePlanBundle("code-699", "label-931")
        val view = toManualLanePlanBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualLanePlanBundleViews(mutableListOf<ManualLanePlanBundle>(item, item)).size, "views")
    }
}
