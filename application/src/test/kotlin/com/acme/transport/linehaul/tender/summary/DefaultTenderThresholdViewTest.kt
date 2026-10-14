package com.acme.transport.linehaul.tender.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultTenderThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultTenderThreshold("code-697", "label-17")
        val view = toDefaultTenderThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultTenderThresholdViews(mutableListOf<DefaultTenderThreshold>(item, item)).size, "views")
    }
}
