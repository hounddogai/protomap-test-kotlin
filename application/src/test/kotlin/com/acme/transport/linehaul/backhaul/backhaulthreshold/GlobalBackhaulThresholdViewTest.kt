package com.acme.transport.linehaul.backhaul.backhaulthreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalBackhaulThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalBackhaulThreshold("code-93", "name-89")
        val view = toGlobalBackhaulThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalBackhaulThresholdViews(mutableListOf<GlobalBackhaulThreshold>(item, item)).size, "views")
    }
}
