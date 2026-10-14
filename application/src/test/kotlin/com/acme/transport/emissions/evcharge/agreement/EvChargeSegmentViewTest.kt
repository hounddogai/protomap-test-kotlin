package com.acme.transport.emissions.evcharge.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class EvChargeSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEvChargeSegment("code-994", "name-385")
        val view = toEvChargeSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEvChargeSegmentViews(mutableListOf<EvChargeSegment>(item, item)).size, "views")
    }
}
