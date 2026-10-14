package com.acme.transport.freightpay.accessorial.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialThreshold("code-739", "name-504")
        val view = toAccessorialThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialThresholdViews(mutableListOf<AccessorialThreshold>(item, item)).size, "views")
    }
}
