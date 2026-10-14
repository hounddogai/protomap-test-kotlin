package com.acme.transport.linehaul.trailer.trailergroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledTrailerManifestTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledTrailerManifest("code-1", "title-63")
        assertEquals(ScheduledTrailerManifestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newScheduledTrailerManifest("code-1", "title-63")
        try {
            item.moveTo(ScheduledTrailerManifestStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledTrailerManifestStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledTrailerManifest("code-1", "title-63")
        item.status = ScheduledTrailerManifestStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledTrailerManifestStatus.PLANNED) }
    }
}
