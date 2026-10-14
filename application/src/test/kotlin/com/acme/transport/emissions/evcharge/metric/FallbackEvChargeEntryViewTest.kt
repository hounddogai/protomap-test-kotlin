package com.acme.transport.emissions.evcharge.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackEvChargeEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackEvChargeEntry("code-64", "label-809")
        val view = toFallbackEvChargeEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackEvChargeEntryViews(mutableListOf<FallbackEvChargeEntry>(item, item)).size, "views")
    }
}
