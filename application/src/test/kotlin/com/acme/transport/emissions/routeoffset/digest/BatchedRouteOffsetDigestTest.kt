package com.acme.transport.emissions.routeoffset.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRouteOffsetDigestTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedRouteOffsetDigest("code-811", "name-822")
        assertEquals(BatchedRouteOffsetDigestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newBatchedRouteOffsetDigest("code-811", "name-822")
        try {
            item.moveTo(BatchedRouteOffsetDigestStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRouteOffsetDigestStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedRouteOffsetDigest("code-811", "name-822")
        item.status = BatchedRouteOffsetDigestStatus.FAILED
        assertFails("move") { item.moveTo(BatchedRouteOffsetDigestStatus.NEW) }
    }
}
