package com.acme.transport.emissions.evcharge.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class EvChargeOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEvChargeOverride("code-179", "label-469")
        val view = toEvChargeOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEvChargeOverrideViews(mutableListOf<EvChargeOverride>(item, item)).size, "views")
    }
}
