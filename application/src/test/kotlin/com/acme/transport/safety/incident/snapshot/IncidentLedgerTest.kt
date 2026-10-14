package com.acme.transport.safety.incident.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newIncidentLedger("code-83", "title-182")
        assertEquals(IncidentLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newIncidentLedger("code-83", "title-182")
        try {
            item.moveTo(IncidentLedgerStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentLedgerStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newIncidentLedger("code-83", "title-182")
        item.status = IncidentLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(IncidentLedgerStatus.CREATED) }
    }
}
