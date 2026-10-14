package com.acme.transport.lastmile.drop.droprequest

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientDropRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientDropRequest("code-901", "title-874")
        val view = toLenientDropRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientDropRequestViews(mutableListOf<LenientDropRequest>(item, item)).size, "views")
    }
}
