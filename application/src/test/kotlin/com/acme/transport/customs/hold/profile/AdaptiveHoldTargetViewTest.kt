package com.acme.transport.customs.hold.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveHoldTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveHoldTarget("code-409", "name-70")
        val view = toAdaptiveHoldTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveHoldTargetViews(mutableListOf<AdaptiveHoldTarget>(item, item)).size, "views")
    }
}
