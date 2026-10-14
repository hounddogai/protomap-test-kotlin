package com.acme.transport.safety.incident.defaultincidentmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyIncidentManifestTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyIncidentManifest("code-980", "title-202")
        assertEquals(LegacyIncidentManifestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newLegacyIncidentManifest("code-980", "title-202")
        try {
            item.moveTo(LegacyIncidentManifestStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyIncidentManifestStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyIncidentManifest("code-980", "title-202")
        item.status = LegacyIncidentManifestStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyIncidentManifestStatus.PLANNED) }
    }
}
