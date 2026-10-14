package com.acme.transport.freightpay.accessorial.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialTier("code-309", "label-866")
        val view = toAccessorialTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialTierViews(mutableListOf<AccessorialTier>(item, item)).size, "views")
    }
}
