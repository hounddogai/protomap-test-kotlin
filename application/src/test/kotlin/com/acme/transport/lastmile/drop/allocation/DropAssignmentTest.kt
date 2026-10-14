package com.acme.transport.lastmile.drop.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropAssignmentTest {
    @Test
    fun `starts in requested`() {
        val item = newDropAssignment("code-255", "label-951")
        assertEquals(DropAssignmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDropAssignment("code-255", "label-951")
        try {
            item.moveTo(DropAssignmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropAssignmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDropAssignment("code-255", "label-951")
        item.status = DropAssignmentStatus.EXPIRED
        assertFails("move") { item.moveTo(DropAssignmentStatus.REQUESTED) }
    }
}
