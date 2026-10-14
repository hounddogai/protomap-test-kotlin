package com.acme.transport.freightpay.accessorial.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveAccessorialCheckpointTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveAccessorialCheckpoint("code-370", "title-62")
        assertEquals(AdaptiveAccessorialCheckpointStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newAdaptiveAccessorialCheckpoint("code-370", "title-62")
        try {
            item.moveTo(AdaptiveAccessorialCheckpointStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveAccessorialCheckpointStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveAccessorialCheckpoint("code-370", "title-62")
        item.status = AdaptiveAccessorialCheckpointStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveAccessorialCheckpointStatus.NEW) }
    }
}
