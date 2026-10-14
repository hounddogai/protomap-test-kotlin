package com.acme.transport.fleetops.tire.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTireLedgerTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedTireLedger("code-807", "name-330")
        assertEquals(WeightedTireLedgerStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newWeightedTireLedger("code-807", "name-330")
        try {
            item.moveTo(WeightedTireLedgerStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTireLedgerStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedTireLedger("code-807", "name-330")
        item.status = WeightedTireLedgerStatus.FAILED
        assertFails("move") { item.moveTo(WeightedTireLedgerStatus.NEW) }
    }
}
