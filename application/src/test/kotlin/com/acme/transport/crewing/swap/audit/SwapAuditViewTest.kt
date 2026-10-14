package com.acme.transport.crewing.swap.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class SwapAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSwapAudit("code-476", "label-874")
        val view = toSwapAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSwapAuditViews(mutableListOf<SwapAudit>(item, item)).size, "views")
    }
}
