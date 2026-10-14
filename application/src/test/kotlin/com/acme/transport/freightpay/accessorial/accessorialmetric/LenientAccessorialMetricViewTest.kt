package com.acme.transport.freightpay.accessorial.accessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientAccessorialMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientAccessorialMetric("code-568", "label-392")
        val view = toLenientAccessorialMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientAccessorialMetricViews(mutableListOf<LenientAccessorialMetric>(item, item)).size, "views")
    }
}
