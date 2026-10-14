package com.acme.transport.safety.violation.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationLedgerTest {
    @Test
    fun `starts in open`() {
        val item = newViolationLedger("code-663", "label-923")
        assertEquals(ViolationLedgerStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newViolationLedger("code-663", "label-923")
        try {
            item.moveTo(ViolationLedgerStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationLedgerStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newViolationLedger("code-663", "label-923")
        item.status = ViolationLedgerStatus.ARCHIVED
        assertFails("move") { item.moveTo(ViolationLedgerStatus.OPEN) }
    }
}
