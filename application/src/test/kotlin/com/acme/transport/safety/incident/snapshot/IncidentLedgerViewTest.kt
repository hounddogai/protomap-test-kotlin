package com.acme.transport.safety.incident.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class IncidentLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIncidentLedger("code-83", "title-182")
        val view = toIncidentLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIncidentLedgerViews(mutableListOf<IncidentLedger>(item, item)).size, "views")
    }
}
