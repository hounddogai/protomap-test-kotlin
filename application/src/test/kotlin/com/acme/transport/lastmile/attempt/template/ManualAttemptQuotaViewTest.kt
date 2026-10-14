package com.acme.transport.lastmile.attempt.template

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualAttemptQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualAttemptQuota("code-574", "title-543")
        val view = toManualAttemptQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualAttemptQuotaViews(mutableListOf<ManualAttemptQuota>(item, item)).size, "views")
    }
}
