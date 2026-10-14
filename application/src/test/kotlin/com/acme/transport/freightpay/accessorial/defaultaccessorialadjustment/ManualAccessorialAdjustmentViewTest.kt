package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualAccessorialAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualAccessorialAdjustment("code-31", "title-345")
        val view = toManualAccessorialAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualAccessorialAdjustmentViews(mutableListOf<ManualAccessorialAdjustment>(item, item)).size, "views")
    }
}
