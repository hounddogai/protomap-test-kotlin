package com.acme.transport.crewing.restbreak.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakManifestTest {
    @Test
    fun `starts in proposed`() {
        val item = newRestBreakManifest("code-867", "label-109")
        assertEquals(RestBreakManifestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRestBreakManifest("code-867", "label-109")
        try {
            item.moveTo(RestBreakManifestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakManifestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRestBreakManifest("code-867", "label-109")
        item.status = RestBreakManifestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RestBreakManifestStatus.PROPOSED) }
    }
}
