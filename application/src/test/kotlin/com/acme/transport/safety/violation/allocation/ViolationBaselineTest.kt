package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationBaselineTest {
    @Test
    fun `starts in draft`() {
        val item = newViolationBaseline("code-856", "label-739")
        assertEquals(ViolationBaselineStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newViolationBaseline("code-856", "label-739")
        try {
            item.moveTo(ViolationBaselineStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationBaselineStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newViolationBaseline("code-856", "label-739")
        item.status = ViolationBaselineStatus.CLOSED
        assertFails("move") { item.moveTo(ViolationBaselineStatus.DRAFT) }
    }
}
