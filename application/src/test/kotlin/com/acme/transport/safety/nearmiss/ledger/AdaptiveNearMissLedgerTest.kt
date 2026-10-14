package com.acme.transport.safety.nearmiss.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveNearMissLedgerTest {
    @Test
    fun `starts in open`() {
        val item = newAdaptiveNearMissLedger("code-503", "label-788")
        assertEquals(AdaptiveNearMissLedgerStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAdaptiveNearMissLedger("code-503", "label-788")
        try {
            item.moveTo(AdaptiveNearMissLedgerStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveNearMissLedgerStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAdaptiveNearMissLedger("code-503", "label-788")
        item.status = AdaptiveNearMissLedgerStatus.ARCHIVED
        assertFails("move") { item.moveTo(AdaptiveNearMissLedgerStatus.OPEN) }
    }
}
