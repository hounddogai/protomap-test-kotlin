package com.acme.transport.customs.inspection.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryInspectionAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryInspectionAudit("code-676", "title-441")
        val view = toPrimaryInspectionAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryInspectionAuditViews(mutableListOf<PrimaryInspectionAudit>(item, item)).size, "views")
    }
}
