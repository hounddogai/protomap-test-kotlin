package com.acme.transport.emissions.routeoffset.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetCheckpointTest {
    @Test
    fun `starts in created`() {
        val item = newRouteOffsetCheckpoint("code-382", "name-390")
        assertEquals(RouteOffsetCheckpointStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRouteOffsetCheckpoint("code-382", "name-390")
        try {
            item.moveTo(RouteOffsetCheckpointStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetCheckpointStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRouteOffsetCheckpoint("code-382", "name-390")
        item.status = RouteOffsetCheckpointStatus.RETIRED
        assertFails("move") { item.moveTo(RouteOffsetCheckpointStatus.CREATED) }
    }
}
