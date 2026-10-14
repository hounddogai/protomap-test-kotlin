package com.acme.transport.freightpay.accessorial.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialTemplate("code-618", "name-9")
        val view = toAccessorialTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialTemplateViews(mutableListOf<AccessorialTemplate>(item, item)).size, "views")
    }
}
