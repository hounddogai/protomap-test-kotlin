package com.acme.transport.crewing.qualification.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryQualificationRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryQualificationRecord("code-409", "label-921")
        val view = toPrimaryQualificationRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryQualificationRecordViews(mutableListOf<PrimaryQualificationRecord>(item, item)).size, "views")
    }
}
