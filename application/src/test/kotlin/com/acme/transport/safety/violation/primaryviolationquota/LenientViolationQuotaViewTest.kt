package com.acme.transport.safety.violation.primaryviolationquota

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientViolationQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientViolationQuota("code-748", "title-20")
        val view = toLenientViolationQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientViolationQuotaViews(mutableListOf<LenientViolationQuota>(item, item)).size, "views")
    }
}
