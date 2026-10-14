package com.acme.transport.freightpay.accessorial.target

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingAccessorialEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingAccessorialEstimate("code-568", "title-245")
        val view = toRollingAccessorialEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingAccessorialEstimateViews(mutableListOf<RollingAccessorialEstimate>(item, item)).size, "views")
    }
}
