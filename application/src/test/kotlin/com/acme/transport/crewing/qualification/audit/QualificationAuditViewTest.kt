package com.acme.transport.crewing.qualification.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class QualificationAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newQualificationAudit("code-959", "label-356")
        val view = toQualificationAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toQualificationAuditViews(mutableListOf<QualificationAudit>(item, item)).size, "views")
    }
}
