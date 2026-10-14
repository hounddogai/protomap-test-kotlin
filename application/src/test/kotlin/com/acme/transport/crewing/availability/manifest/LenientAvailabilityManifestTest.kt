package com.acme.transport.crewing.availability.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientAvailabilityManifestTest {
    @Test
    fun `starts in idle`() {
        val item = newLenientAvailabilityManifest("code-942", "title-523")
        assertEquals(LenientAvailabilityManifestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLenientAvailabilityManifest("code-942", "title-523")
        try {
            item.moveTo(LenientAvailabilityManifestStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientAvailabilityManifestStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLenientAvailabilityManifest("code-942", "title-523")
        item.status = LenientAvailabilityManifestStatus.STOPPED
        assertFails("move") { item.moveTo(LenientAvailabilityManifestStatus.IDLE) }
    }
}
