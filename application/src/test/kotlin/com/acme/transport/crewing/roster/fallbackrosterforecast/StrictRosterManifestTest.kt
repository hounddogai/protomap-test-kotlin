package com.acme.transport.crewing.roster.fallbackrosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRosterManifestTest {
    @Test
    fun `starts in requested`() {
        val item = newStrictRosterManifest("code-62", "label-367")
        assertEquals(StrictRosterManifestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newStrictRosterManifest("code-62", "label-367")
        try {
            item.moveTo(StrictRosterManifestStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRosterManifestStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newStrictRosterManifest("code-62", "label-367")
        item.status = StrictRosterManifestStatus.EXPIRED
        assertFails("move") { item.moveTo(StrictRosterManifestStatus.REQUESTED) }
    }
}
