package com.acme.transport.crewing.qualification.qualificationtemplate

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryQualificationLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryQualificationLedger("code-40", "title-531")
        val view = toPrimaryQualificationLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryQualificationLedgerViews(mutableListOf<PrimaryQualificationLedger>(item, item)).size, "views")
    }
}
