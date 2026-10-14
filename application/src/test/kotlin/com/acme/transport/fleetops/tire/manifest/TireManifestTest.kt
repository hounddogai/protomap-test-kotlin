package com.acme.transport.fleetops.tire.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireManifestTest {
    @Test
    fun `starts in proposed`() {
        val item = newTireManifest("code-58", "name-355")
        assertEquals(TireManifestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTireManifest("code-58", "name-355")
        try {
            item.moveTo(TireManifestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireManifestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTireManifest("code-58", "name-355")
        item.status = TireManifestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TireManifestStatus.PROPOSED) }
    }
}
