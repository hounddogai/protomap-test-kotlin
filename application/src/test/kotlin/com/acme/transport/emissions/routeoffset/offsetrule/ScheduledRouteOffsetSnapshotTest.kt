package com.acme.transport.emissions.routeoffset.offsetrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledRouteOffsetSnapshotTest {
    @Test
    fun `starts in proposed`() {
        val item = newScheduledRouteOffsetSnapshot("code-611", "label-504")
        assertEquals(ScheduledRouteOffsetSnapshotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newScheduledRouteOffsetSnapshot("code-611", "label-504")
        try {
            item.moveTo(ScheduledRouteOffsetSnapshotStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledRouteOffsetSnapshotStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScheduledRouteOffsetSnapshot("code-611", "label-504")
        item.status = ScheduledRouteOffsetSnapshotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScheduledRouteOffsetSnapshotStatus.PROPOSED) }
    }
}
