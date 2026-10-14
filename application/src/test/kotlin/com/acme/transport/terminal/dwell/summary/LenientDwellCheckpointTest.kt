package com.acme.transport.terminal.dwell.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDwellCheckpointTest {
    @Test
    fun `starts in requested`() {
        val item = newLenientDwellCheckpoint("code-950", "label-423")
        assertEquals(LenientDwellCheckpointStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newLenientDwellCheckpoint("code-950", "label-423")
        try {
            item.moveTo(LenientDwellCheckpointStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDwellCheckpointStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLenientDwellCheckpoint("code-950", "label-423")
        item.status = LenientDwellCheckpointStatus.EXPIRED
        assertFails("move") { item.moveTo(LenientDwellCheckpointStatus.REQUESTED) }
    }
}
