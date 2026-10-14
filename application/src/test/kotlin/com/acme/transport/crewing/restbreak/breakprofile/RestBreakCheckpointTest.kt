package com.acme.transport.crewing.restbreak.breakprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakCheckpointTest {
    @Test
    fun `starts in idle`() {
        val item = newRestBreakCheckpoint("code-134", "title-913")
        assertEquals(RestBreakCheckpointStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRestBreakCheckpoint("code-134", "title-913")
        try {
            item.moveTo(RestBreakCheckpointStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakCheckpointStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRestBreakCheckpoint("code-134", "title-913")
        item.status = RestBreakCheckpointStatus.STOPPED
        assertFails("move") { item.moveTo(RestBreakCheckpointStatus.IDLE) }
    }
}
