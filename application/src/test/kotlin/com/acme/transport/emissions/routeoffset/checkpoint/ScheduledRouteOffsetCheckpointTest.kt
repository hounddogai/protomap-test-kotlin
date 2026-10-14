package com.acme.transport.emissions.routeoffset.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledRouteOffsetCheckpointTest {
    @Test
    fun `starts in requested`() {
        val item = newScheduledRouteOffsetCheckpoint("code-474", "name-407")
        assertEquals(ScheduledRouteOffsetCheckpointStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newScheduledRouteOffsetCheckpoint("code-474", "name-407")
        try {
            item.moveTo(ScheduledRouteOffsetCheckpointStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledRouteOffsetCheckpointStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScheduledRouteOffsetCheckpoint("code-474", "name-407")
        item.status = ScheduledRouteOffsetCheckpointStatus.EXPIRED
        assertFails("move") { item.moveTo(ScheduledRouteOffsetCheckpointStatus.REQUESTED) }
    }
}
