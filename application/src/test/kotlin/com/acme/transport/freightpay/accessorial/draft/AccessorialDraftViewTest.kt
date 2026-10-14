package com.acme.transport.freightpay.accessorial.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialDraft("code-878", "name-769")
        val view = toAccessorialDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialDraftViews(mutableListOf<AccessorialDraft>(item, item)).size, "views")
    }
}
