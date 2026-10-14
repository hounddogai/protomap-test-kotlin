package com.acme.transport.crewing.qualification.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyQualificationQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyQualificationQuota("code-897", "title-122")
        val view = toLegacyQualificationQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyQualificationQuotaViews(mutableListOf<LegacyQualificationQuota>(item, item)).size, "views")
    }
}
