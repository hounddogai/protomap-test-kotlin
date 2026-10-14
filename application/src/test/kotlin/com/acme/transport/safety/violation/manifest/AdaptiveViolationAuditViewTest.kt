package com.acme.transport.safety.violation.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveViolationAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveViolationAudit("code-317", "name-944")
        val view = toAdaptiveViolationAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveViolationAuditViews(mutableListOf<AdaptiveViolationAudit>(item, item)).size, "views")
    }
}
