package com.acme.transport.freightpay.accessorial.accessorialplan

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialEstimate("code-961", "title-650")
        val view = toAccessorialEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialEstimateViews(mutableListOf<AccessorialEstimate>(item, item)).size, "views")
    }
}
