package com.acme.transport.freightpay.accessorial.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialProfile("code-219", "name-459")
        val view = toAccessorialProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialProfileViews(mutableListOf<AccessorialProfile>(item, item)).size, "views")
    }
}
