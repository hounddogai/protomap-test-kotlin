package com.acme.transport.crewing.qualification.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class QualificationLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newQualificationLedger("code-784", "name-388")
        val view = toQualificationLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toQualificationLedgerViews(mutableListOf<QualificationLedger>(item, item)).size, "views")
    }
}
