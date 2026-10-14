package com.acme.transport.lastmile.attempt.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientAttemptLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientAttemptLedger("code-838", "title-216")
        val view = toLenientAttemptLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientAttemptLedgerViews(mutableListOf<LenientAttemptLedger>(item, item)).size, "views")
    }
}
