package com.acme.transport.safety.coaching.template

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryCoachingLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryCoachingLedger("code-47", "title-161")
        val view = toPrimaryCoachingLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryCoachingLedgerViews(mutableListOf<PrimaryCoachingLedger>(item, item)).size, "views")
    }
}
