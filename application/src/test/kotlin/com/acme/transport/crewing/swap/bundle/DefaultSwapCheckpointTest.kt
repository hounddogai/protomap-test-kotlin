package com.acme.transport.crewing.swap.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultSwapCheckpointTest {
    @Test
    fun `starts in planned`() {
        val item = newDefaultSwapCheckpoint("code-655", "title-203")
        assertEquals(DefaultSwapCheckpointStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newDefaultSwapCheckpoint("code-655", "title-203")
        try {
            item.moveTo(DefaultSwapCheckpointStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultSwapCheckpointStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefaultSwapCheckpoint("code-655", "title-203")
        item.status = DefaultSwapCheckpointStatus.CANCELLED
        assertFails("move") { item.moveTo(DefaultSwapCheckpointStatus.PLANNED) }
    }
}
