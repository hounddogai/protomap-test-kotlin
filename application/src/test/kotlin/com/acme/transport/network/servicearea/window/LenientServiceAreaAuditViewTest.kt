package com.acme.transport.network.servicearea.window

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientServiceAreaAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientServiceAreaAudit("code-924", "title-640")
        val view = toLenientServiceAreaAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientServiceAreaAuditViews(mutableListOf<LenientServiceAreaAudit>(item, item)).size, "views")
    }
}
