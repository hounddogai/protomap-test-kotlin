package com.acme.transport.safety.violation.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedViolationBaselineTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedViolationBaseline("code-659", "name-781")
        assertEquals(CappedViolationBaselineStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCappedViolationBaseline("code-659", "name-781")
        try {
            item.moveTo(CappedViolationBaselineStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedViolationBaselineStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedViolationBaseline("code-659", "name-781")
        item.status = CappedViolationBaselineStatus.CLOSED
        assertFails("move") { item.moveTo(CappedViolationBaselineStatus.DRAFT) }
    }
}
