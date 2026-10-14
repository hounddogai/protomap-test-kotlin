package com.acme.transport.safety.cameraevent.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedCameraEventLedgerTest {
    @Test
    fun `starts in proposed`() {
        val item = newWeightedCameraEventLedger("code-358", "label-544")
        assertEquals(WeightedCameraEventLedgerStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newWeightedCameraEventLedger("code-358", "label-544")
        try {
            item.moveTo(WeightedCameraEventLedgerStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedCameraEventLedgerStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newWeightedCameraEventLedger("code-358", "label-544")
        item.status = WeightedCameraEventLedgerStatus.WITHDRAWN
        assertFails("move") { item.moveTo(WeightedCameraEventLedgerStatus.PROPOSED) }
    }
}
