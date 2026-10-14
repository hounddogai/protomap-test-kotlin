package com.acme.transport.safety.coaching.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingManifestTest {
    @Test
    fun `starts in draft`() {
        val item = newCoachingManifest("code-169", "name-917")
        assertEquals(CoachingManifestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newCoachingManifest("code-169", "name-917")
        try {
            item.moveTo(CoachingManifestStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingManifestStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCoachingManifest("code-169", "name-917")
        item.status = CoachingManifestStatus.CLOSED
        assertFails("move") { item.moveTo(CoachingManifestStatus.DRAFT) }
    }
}
