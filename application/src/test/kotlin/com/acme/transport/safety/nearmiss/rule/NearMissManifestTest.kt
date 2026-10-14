package com.acme.transport.safety.nearmiss.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissManifestTest {
    @Test
    fun `starts in new`() {
        val item = newNearMissManifest("code-431", "name-836")
        assertEquals(NearMissManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newNearMissManifest("code-431", "name-836")
        try {
            item.moveTo(NearMissManifestStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissManifestStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newNearMissManifest("code-431", "name-836")
        item.status = NearMissManifestStatus.FAILED
        assertFails("move") { item.moveTo(NearMissManifestStatus.NEW) }
    }
}
