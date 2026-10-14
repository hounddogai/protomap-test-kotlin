package com.acme.transport.safety.coaching.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingCoachingLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingCoachingLedger("code-292", "title-430")
        val view = toPendingCoachingLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingCoachingLedgerViews(mutableListOf<PendingCoachingLedger>(item, item)).size, "views")
    }
}
