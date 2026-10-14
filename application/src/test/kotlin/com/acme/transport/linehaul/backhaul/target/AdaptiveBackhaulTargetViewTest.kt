package com.acme.transport.linehaul.backhaul.target

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveBackhaulTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveBackhaulTarget("code-292", "label-103")
        val view = toAdaptiveBackhaulTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveBackhaulTargetViews(mutableListOf<AdaptiveBackhaulTarget>(item, item)).size, "views")
    }
}
