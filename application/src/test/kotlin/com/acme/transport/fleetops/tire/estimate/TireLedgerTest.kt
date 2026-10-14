package com.acme.transport.fleetops.tire.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newTireLedger("code-979", "name-920")
        assertEquals(TireLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newTireLedger("code-979", "name-920")
        try {
            item.moveTo(TireLedgerStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireLedgerStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTireLedger("code-979", "name-920")
        item.status = TireLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(TireLedgerStatus.CREATED) }
    }
}
