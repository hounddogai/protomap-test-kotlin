package com.acme.transport.safety.incident.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentManifestTest {
    @Test
    fun `starts in proposed`() {
        val item = newIncidentManifest("code-167", "title-177")
        assertEquals(IncidentManifestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newIncidentManifest("code-167", "title-177")
        try {
            item.moveTo(IncidentManifestStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentManifestStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newIncidentManifest("code-167", "title-177")
        item.status = IncidentManifestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(IncidentManifestStatus.PROPOSED) }
    }
}
