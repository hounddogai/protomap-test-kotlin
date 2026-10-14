package com.acme.transport.crewing.availability.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityManifestTest {
    @Test
    fun `starts in idle`() {
        val item = newAvailabilityManifest("code-904", "name-90")
        assertEquals(AvailabilityManifestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newAvailabilityManifest("code-904", "name-90")
        try {
            item.moveTo(AvailabilityManifestStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityManifestStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAvailabilityManifest("code-904", "name-90")
        item.status = AvailabilityManifestStatus.STOPPED
        assertFails("move") { item.moveTo(AvailabilityManifestStatus.IDLE) }
    }
}
