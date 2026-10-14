package com.acme.transport.safety.coaching.review

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualCoachingLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualCoachingLedger("code-376", "title-586")
        val view = toManualCoachingLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualCoachingLedgerViews(mutableListOf<ManualCoachingLedger>(item, item)).size, "views")
    }
}
