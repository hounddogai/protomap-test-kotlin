package com.acme.transport.fleetops.serviceplan.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingServicePlanLedgerTest {
    @Test
    fun `starts in open`() {
        val item = newRollingServicePlanLedger("code-45", "name-698")
        assertEquals(RollingServicePlanLedgerStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRollingServicePlanLedger("code-45", "name-698")
        try {
            item.moveTo(RollingServicePlanLedgerStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingServicePlanLedgerStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingServicePlanLedger("code-45", "name-698")
        item.status = RollingServicePlanLedgerStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingServicePlanLedgerStatus.OPEN) }
    }
}
