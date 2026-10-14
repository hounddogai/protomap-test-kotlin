package com.acme.transport.customs.clearance.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyClearanceCheckpointTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyClearanceCheckpoint("code-47", "name-356")
        assertEquals(LegacyClearanceCheckpointStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLegacyClearanceCheckpoint("code-47", "name-356")
        try {
            item.moveTo(LegacyClearanceCheckpointStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyClearanceCheckpointStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyClearanceCheckpoint("code-47", "name-356")
        item.status = LegacyClearanceCheckpointStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyClearanceCheckpointStatus.IDLE) }
    }
}
