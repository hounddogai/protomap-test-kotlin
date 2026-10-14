package com.acme.transport.safety.violation.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationEntryTest {
    @Test
    fun `starts in requested`() {
        val item = newViolationEntry("code-982", "name-88")
        assertEquals(ViolationEntryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newViolationEntry("code-982", "name-88")
        try {
            item.moveTo(ViolationEntryStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationEntryStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newViolationEntry("code-982", "name-88")
        item.status = ViolationEntryStatus.EXPIRED
        assertFails("move") { item.moveTo(ViolationEntryStatus.REQUESTED) }
    }
}
