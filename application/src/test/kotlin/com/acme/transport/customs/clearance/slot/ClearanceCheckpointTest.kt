package com.acme.transport.customs.clearance.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceCheckpointTest {
    @Test
    fun `starts in requested`() {
        val item = newClearanceCheckpoint("code-451", "name-638")
        assertEquals(ClearanceCheckpointStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newClearanceCheckpoint("code-451", "name-638")
        try {
            item.moveTo(ClearanceCheckpointStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceCheckpointStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newClearanceCheckpoint("code-451", "name-638")
        item.status = ClearanceCheckpointStatus.EXPIRED
        assertFails("move") { item.moveTo(ClearanceCheckpointStatus.REQUESTED) }
    }
}
