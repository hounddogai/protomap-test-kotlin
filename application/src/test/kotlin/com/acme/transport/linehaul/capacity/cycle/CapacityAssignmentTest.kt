package com.acme.transport.linehaul.capacity.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityAssignmentTest {
    @Test
    fun `starts in requested`() {
        val item = newCapacityAssignment("code-863", "name-361")
        assertEquals(CapacityAssignmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCapacityAssignment("code-863", "name-361")
        try {
            item.moveTo(CapacityAssignmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityAssignmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCapacityAssignment("code-863", "name-361")
        item.status = CapacityAssignmentStatus.EXPIRED
        assertFails("move") { item.moveTo(CapacityAssignmentStatus.REQUESTED) }
    }
}
