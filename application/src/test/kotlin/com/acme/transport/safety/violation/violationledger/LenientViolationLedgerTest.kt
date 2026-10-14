package com.acme.transport.safety.violation.violationledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientViolationLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newLenientViolationLedger("code-888", "name-226")
        assertEquals(LenientViolationLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLenientViolationLedger("code-888", "name-226")
        try {
            item.moveTo(LenientViolationLedgerStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientViolationLedgerStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientViolationLedger("code-888", "name-226")
        item.status = LenientViolationLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(LenientViolationLedgerStatus.CREATED) }
    }
}
