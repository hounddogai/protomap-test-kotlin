package com.acme.transport.emissions.routeoffset.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingRouteOffsetDigestTest {
    @Test
    fun `starts in new`() {
        val item = newPendingRouteOffsetDigest("code-173", "title-52")
        assertEquals(PendingRouteOffsetDigestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newPendingRouteOffsetDigest("code-173", "title-52")
        try {
            item.moveTo(PendingRouteOffsetDigestStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingRouteOffsetDigestStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPendingRouteOffsetDigest("code-173", "title-52")
        item.status = PendingRouteOffsetDigestStatus.FAILED
        assertFails("move") { item.moveTo(PendingRouteOffsetDigestStatus.NEW) }
    }
}
