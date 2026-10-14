package com.acme.transport.network.transittime.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictTransitTimeDigestTest {
    @Test
    fun `starts in new`() {
        val item = newStrictTransitTimeDigest("code-99", "title-838")
        assertEquals(StrictTransitTimeDigestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newStrictTransitTimeDigest("code-99", "title-838")
        try {
            item.moveTo(StrictTransitTimeDigestStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictTransitTimeDigestStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictTransitTimeDigest("code-99", "title-838")
        item.status = StrictTransitTimeDigestStatus.FAILED
        assertFails("move") { item.moveTo(StrictTransitTimeDigestStatus.NEW) }
    }
}
