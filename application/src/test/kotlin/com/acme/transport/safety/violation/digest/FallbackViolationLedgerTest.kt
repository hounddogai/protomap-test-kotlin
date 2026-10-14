package com.acme.transport.safety.violation.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackViolationLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackViolationLedger("code-147", "label-738")
        assertEquals(FallbackViolationLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newFallbackViolationLedger("code-147", "label-738")
        try {
            item.moveTo(FallbackViolationLedgerStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackViolationLedgerStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackViolationLedger("code-147", "label-738")
        item.status = FallbackViolationLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackViolationLedgerStatus.CREATED) }
    }
}
