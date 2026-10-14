package com.acme.transport.fleetops.serviceplan.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackServicePlanLedgerTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackServicePlanLedger("code-941", "title-50")
        assertEquals(FallbackServicePlanLedgerStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newFallbackServicePlanLedger("code-941", "title-50")
        try {
            item.moveTo(FallbackServicePlanLedgerStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackServicePlanLedgerStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackServicePlanLedger("code-941", "title-50")
        item.status = FallbackServicePlanLedgerStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackServicePlanLedgerStatus.PROPOSED) }
    }
}
