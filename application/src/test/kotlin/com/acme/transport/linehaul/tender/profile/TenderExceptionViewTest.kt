package com.acme.transport.linehaul.tender.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderException("code-800", "name-948")
        val view = toTenderExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderExceptionViews(mutableListOf<TenderException>(item, item)).size, "views")
    }
}
