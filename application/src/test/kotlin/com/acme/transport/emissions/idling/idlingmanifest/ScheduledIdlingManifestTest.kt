package com.acme.transport.emissions.idling.idlingmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledIdlingManifestTest {
    @Test
    fun `starts in proposed`() {
        val item = newScheduledIdlingManifest("code-122", "title-616")
        assertEquals(ScheduledIdlingManifestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newScheduledIdlingManifest("code-122", "title-616")
        try {
            item.moveTo(ScheduledIdlingManifestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledIdlingManifestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScheduledIdlingManifest("code-122", "title-616")
        item.status = ScheduledIdlingManifestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScheduledIdlingManifestStatus.PROPOSED) }
    }
}
