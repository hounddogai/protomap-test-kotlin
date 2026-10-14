package com.acme.transport.lastmile.attempt.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptCheckpointTest {
    @Test
    fun `starts in requested`() {
        val item = newAttemptCheckpoint("code-153", "title-995")
        assertEquals(AttemptCheckpointStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newAttemptCheckpoint("code-153", "title-995")
        try {
            item.moveTo(AttemptCheckpointStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptCheckpointStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAttemptCheckpoint("code-153", "title-995")
        item.status = AttemptCheckpointStatus.EXPIRED
        assertFails("move") { item.moveTo(AttemptCheckpointStatus.REQUESTED) }
    }
}
