package com.acme.transport.freightpay.audit.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditException("code-720", "name-896")
        val view = toAuditExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditExceptionViews(mutableListOf<AuditException>(item, item)).size, "views")
    }
}
