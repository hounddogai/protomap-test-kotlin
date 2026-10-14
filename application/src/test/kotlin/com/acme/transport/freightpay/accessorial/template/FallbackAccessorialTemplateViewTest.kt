package com.acme.transport.freightpay.accessorial.template

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackAccessorialTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackAccessorialTemplate("code-278", "label-35")
        val view = toFallbackAccessorialTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackAccessorialTemplateViews(mutableListOf<FallbackAccessorialTemplate>(item, item)).size, "views")
    }
}
