package com.acme.transport.network.transittime.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeDigestTest {
    @Test
    fun `starts in requested`() {
        val item = newTransitTimeDigest("code-764", "label-309")
        assertEquals(TransitTimeDigestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newTransitTimeDigest("code-764", "label-309")
        try {
            item.moveTo(TransitTimeDigestStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeDigestStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTransitTimeDigest("code-764", "label-309")
        item.status = TransitTimeDigestStatus.EXPIRED
        assertFails("move") { item.moveTo(TransitTimeDigestStatus.REQUESTED) }
    }
}
