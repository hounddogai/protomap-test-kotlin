package com.acme.transport.safety.nearmiss.missvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissCheckpointTest {
    @Test
    fun `starts in created`() {
        val item = newNearMissCheckpoint("code-857", "label-326")
        assertEquals(NearMissCheckpointStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newNearMissCheckpoint("code-857", "label-326")
        try {
            item.moveTo(NearMissCheckpointStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissCheckpointStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newNearMissCheckpoint("code-857", "label-326")
        item.status = NearMissCheckpointStatus.RETIRED
        assertFails("move") { item.moveTo(NearMissCheckpointStatus.CREATED) }
    }
}
