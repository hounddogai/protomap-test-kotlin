package com.acme.transport.linehaul.laneplan.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingLanePlanLedgerTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingLanePlanLedger("code-530", "title-304")
        assertEquals(RollingLanePlanLedgerStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newRollingLanePlanLedger("code-530", "title-304")
        try {
            item.moveTo(RollingLanePlanLedgerStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingLanePlanLedgerStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingLanePlanLedger("code-530", "title-304")
        item.status = RollingLanePlanLedgerStatus.CLOSED
        assertFails("move") { item.moveTo(RollingLanePlanLedgerStatus.DRAFT) }
    }
}
