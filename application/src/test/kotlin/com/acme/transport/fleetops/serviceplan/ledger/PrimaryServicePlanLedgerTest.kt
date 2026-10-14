package com.acme.transport.fleetops.serviceplan.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryServicePlanLedgerTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryServicePlanLedger("code-634", "label-912")
        assertEquals(PrimaryServicePlanLedgerStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPrimaryServicePlanLedger("code-634", "label-912")
        try {
            item.moveTo(PrimaryServicePlanLedgerStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryServicePlanLedgerStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryServicePlanLedger("code-634", "label-912")
        item.status = PrimaryServicePlanLedgerStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryServicePlanLedgerStatus.PROPOSED) }
    }
}
