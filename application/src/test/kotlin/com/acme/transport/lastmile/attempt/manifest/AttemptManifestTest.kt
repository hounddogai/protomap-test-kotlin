package com.acme.transport.lastmile.attempt.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptManifestTest {
    @Test
    fun `starts in open`() {
        val item = newAttemptManifest("code-386", "name-687")
        assertEquals(AttemptManifestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newAttemptManifest("code-386", "name-687")
        try {
            item.moveTo(AttemptManifestStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptManifestStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAttemptManifest("code-386", "name-687")
        item.status = AttemptManifestStatus.ARCHIVED
        assertFails("move") { item.moveTo(AttemptManifestStatus.OPEN) }
    }
}
