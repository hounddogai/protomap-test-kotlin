package com.acme.transport.emissions.routeoffset.offsetestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingRouteOffsetProfileTest {
    @Test
    fun `starts in new`() {
        val item = newPendingRouteOffsetProfile("code-864", "title-763")
        assertEquals(PendingRouteOffsetProfileStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newPendingRouteOffsetProfile("code-864", "title-763")
        try {
            item.moveTo(PendingRouteOffsetProfileStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingRouteOffsetProfileStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPendingRouteOffsetProfile("code-864", "title-763")
        item.status = PendingRouteOffsetProfileStatus.FAILED
        assertFails("move") { item.moveTo(PendingRouteOffsetProfileStatus.NEW) }
    }
}
