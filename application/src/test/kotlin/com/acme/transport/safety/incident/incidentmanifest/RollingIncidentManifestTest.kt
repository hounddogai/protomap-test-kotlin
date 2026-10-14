package com.acme.transport.safety.incident.incidentmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingIncidentManifestTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingIncidentManifest("code-779", "title-492")
        assertEquals(RollingIncidentManifestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRollingIncidentManifest("code-779", "title-492")
        try {
            item.moveTo(RollingIncidentManifestStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingIncidentManifestStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingIncidentManifest("code-779", "title-492")
        item.status = RollingIncidentManifestStatus.CLOSED
        assertFails("move") { item.moveTo(RollingIncidentManifestStatus.DRAFT) }
    }
}
