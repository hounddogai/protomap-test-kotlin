package com.acme.transport.freightpay.audit.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackAuditTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackAuditTarget("code-282", "name-751")
        val view = toFallbackAuditTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackAuditTargetViews(mutableListOf<FallbackAuditTarget>(item, item)).size, "views")
    }
}
