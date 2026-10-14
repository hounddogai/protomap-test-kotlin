package com.acme.transport.linehaul.tender.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderThreshold("code-796", "name-796")
        val view = toTenderThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderThresholdViews(mutableListOf<TenderThreshold>(item, item)).size, "views")
    }
}
