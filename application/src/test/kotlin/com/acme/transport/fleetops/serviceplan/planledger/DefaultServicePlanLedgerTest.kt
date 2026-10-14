package com.acme.transport.fleetops.serviceplan.planledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultServicePlanLedgerTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultServicePlanLedger("code-802", "label-248")
        assertEquals(DefaultServicePlanLedgerStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newDefaultServicePlanLedger("code-802", "label-248")
        try {
            item.moveTo(DefaultServicePlanLedgerStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultServicePlanLedgerStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultServicePlanLedger("code-802", "label-248")
        item.status = DefaultServicePlanLedgerStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultServicePlanLedgerStatus.REQUESTED) }
    }
}
