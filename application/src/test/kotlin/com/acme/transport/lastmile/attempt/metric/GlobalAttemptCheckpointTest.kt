package com.acme.transport.lastmile.attempt.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalAttemptCheckpointTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalAttemptCheckpoint("code-18", "label-246")
        assertEquals(GlobalAttemptCheckpointStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newGlobalAttemptCheckpoint("code-18", "label-246")
        try {
            item.moveTo(GlobalAttemptCheckpointStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalAttemptCheckpointStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalAttemptCheckpoint("code-18", "label-246")
        item.status = GlobalAttemptCheckpointStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalAttemptCheckpointStatus.CREATED) }
    }
}
