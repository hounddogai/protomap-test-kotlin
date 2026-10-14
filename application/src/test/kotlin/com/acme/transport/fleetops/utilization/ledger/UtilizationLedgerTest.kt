package com.acme.transport.fleetops.utilization.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newUtilizationLedger("code-634", "title-944")
        assertEquals(UtilizationLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newUtilizationLedger("code-634", "title-944")
        try {
            item.moveTo(UtilizationLedgerStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationLedgerStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newUtilizationLedger("code-634", "title-944")
        item.status = UtilizationLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(UtilizationLedgerStatus.CREATED) }
    }
}
