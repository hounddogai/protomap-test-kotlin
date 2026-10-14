package com.acme.transport.emissions.evcharge.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingEvChargeOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingEvChargeOverride("code-557", "label-897")
        val view = toPendingEvChargeOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingEvChargeOverrideViews(mutableListOf<PendingEvChargeOverride>(item, item)).size, "views")
    }
}
