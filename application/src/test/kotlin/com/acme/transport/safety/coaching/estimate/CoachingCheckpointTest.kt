package com.acme.transport.safety.coaching.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingCheckpointTest {
    @Test
    fun `starts in created`() {
        val item = newCoachingCheckpoint("code-337", "name-185")
        assertEquals(CoachingCheckpointStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCoachingCheckpoint("code-337", "name-185")
        try {
            item.moveTo(CoachingCheckpointStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingCheckpointStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCoachingCheckpoint("code-337", "name-185")
        item.status = CoachingCheckpointStatus.RETIRED
        assertFails("move") { item.moveTo(CoachingCheckpointStatus.CREATED) }
    }
}
