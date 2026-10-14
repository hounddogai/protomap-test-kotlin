package com.acme.transport.crewing.qualification.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryQualificationQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryQualificationQuota("code-427", "title-946")
        val view = toPrimaryQualificationQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryQualificationQuotaViews(mutableListOf<PrimaryQualificationQuota>(item, item)).size, "views")
    }
}
