package com.acme.transport.crewing.swap.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapCheckpointTest {
    @Test
    fun `starts in open`() {
        val item = newSwapCheckpoint("code-593", "name-528")
        assertEquals(SwapCheckpointStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newSwapCheckpoint("code-593", "name-528")
        try {
            item.moveTo(SwapCheckpointStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapCheckpointStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSwapCheckpoint("code-593", "name-528")
        item.status = SwapCheckpointStatus.ARCHIVED
        assertFails("move") { item.moveTo(SwapCheckpointStatus.OPEN) }
    }
}
