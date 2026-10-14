package com.acme.transport.emissions.routeoffset.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetSnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newRouteOffsetSnapshot("code-520", "name-564")
        assertEquals(RouteOffsetSnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newRouteOffsetSnapshot("code-520", "name-564")
        try {
            item.moveTo(RouteOffsetSnapshotStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetSnapshotStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRouteOffsetSnapshot("code-520", "name-564")
        item.status = RouteOffsetSnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(RouteOffsetSnapshotStatus.IDLE) }
    }
}
