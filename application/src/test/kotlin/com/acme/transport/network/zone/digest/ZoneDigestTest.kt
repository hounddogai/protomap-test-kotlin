package com.acme.transport.network.zone.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneDigestTest {
    @Test
    fun `starts in planned`() {
        val item = newZoneDigest("code-597", "title-497")
        assertEquals(ZoneDigestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newZoneDigest("code-597", "title-497")
        try {
            item.moveTo(ZoneDigestStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneDigestStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newZoneDigest("code-597", "title-497")
        item.status = ZoneDigestStatus.CANCELLED
        assertFails("move") { item.moveTo(ZoneDigestStatus.PLANNED) }
    }
}
