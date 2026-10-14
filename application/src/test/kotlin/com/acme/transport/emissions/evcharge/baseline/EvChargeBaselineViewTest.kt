package com.acme.transport.emissions.evcharge.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class EvChargeBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEvChargeBaseline("code-489", "label-307")
        val view = toEvChargeBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEvChargeBaselineViews(mutableListOf<EvChargeBaseline>(item, item)).size, "views")
    }
}
