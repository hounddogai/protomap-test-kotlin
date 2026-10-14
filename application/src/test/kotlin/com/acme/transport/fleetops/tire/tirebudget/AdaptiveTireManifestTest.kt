package com.acme.transport.fleetops.tire.tirebudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveTireManifestTest {
    @Test
    fun `starts in open`() {
        val item = newAdaptiveTireManifest("code-654", "title-867")
        assertEquals(AdaptiveTireManifestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAdaptiveTireManifest("code-654", "title-867")
        try {
            item.moveTo(AdaptiveTireManifestStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveTireManifestStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAdaptiveTireManifest("code-654", "title-867")
        item.status = AdaptiveTireManifestStatus.ARCHIVED
        assertFails("move") { item.moveTo(AdaptiveTireManifestStatus.OPEN) }
    }
}
