package com.acme.transport.freightpay.accessorial.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialVariance("code-460", "label-350")
        val view = toAccessorialVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialVarianceViews(mutableListOf<AccessorialVariance>(item, item)).size, "views")
    }
}
