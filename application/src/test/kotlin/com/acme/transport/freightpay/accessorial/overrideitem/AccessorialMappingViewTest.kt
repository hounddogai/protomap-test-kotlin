package com.acme.transport.freightpay.accessorial.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialMapping("code-567", "label-451")
        val view = toAccessorialMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialMappingViews(mutableListOf<AccessorialMapping>(item, item)).size, "views")
    }
}
