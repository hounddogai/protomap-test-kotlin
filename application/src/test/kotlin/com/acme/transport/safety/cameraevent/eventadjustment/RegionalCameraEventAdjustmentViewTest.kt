package com.acme.transport.safety.cameraevent.eventadjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalCameraEventAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalCameraEventAdjustment("code-72", "name-999")
        val view = toRegionalCameraEventAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalCameraEventAdjustmentViews(mutableListOf<RegionalCameraEventAdjustment>(item, item)).size, "views")
    }
}
