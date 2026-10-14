package com.acme.transport.fleetops.serviceplan.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingServicePlanLedgerTest {
    @Test
    fun `starts in idle`() {
        val item = newPendingServicePlanLedger("code-385", "label-446")
        assertEquals(PendingServicePlanLedgerStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newPendingServicePlanLedger("code-385", "label-446")
        try {
            item.moveTo(PendingServicePlanLedgerStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingServicePlanLedgerStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPendingServicePlanLedger("code-385", "label-446")
        item.status = PendingServicePlanLedgerStatus.STOPPED
        assertFails("move") { item.moveTo(PendingServicePlanLedgerStatus.IDLE) }
    }
}
