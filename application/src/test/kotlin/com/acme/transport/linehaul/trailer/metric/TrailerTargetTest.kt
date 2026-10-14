package com.acme.transport.linehaul.trailer.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerTargetTest {
    @Test
    fun `starts in idle`() {
        val item = newTrailerTarget("code-349", "label-502")
        assertEquals(TrailerTargetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newTrailerTarget("code-349", "label-502")
        try {
            item.moveTo(TrailerTargetStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerTargetStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTrailerTarget("code-349", "label-502")
        item.status = TrailerTargetStatus.STOPPED
        assertFails("move") { item.moveTo(TrailerTargetStatus.IDLE) }
    }
}
