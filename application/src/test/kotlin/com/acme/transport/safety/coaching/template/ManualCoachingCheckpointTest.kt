package com.acme.transport.safety.coaching.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCoachingCheckpointTest {
    @Test
    fun `starts in created`() {
        val item = newManualCoachingCheckpoint("code-354", "label-315")
        assertEquals(ManualCoachingCheckpointStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newManualCoachingCheckpoint("code-354", "label-315")
        try {
            item.moveTo(ManualCoachingCheckpointStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCoachingCheckpointStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualCoachingCheckpoint("code-354", "label-315")
        item.status = ManualCoachingCheckpointStatus.RETIRED
        assertFails("move") { item.moveTo(ManualCoachingCheckpointStatus.CREATED) }
    }
}
