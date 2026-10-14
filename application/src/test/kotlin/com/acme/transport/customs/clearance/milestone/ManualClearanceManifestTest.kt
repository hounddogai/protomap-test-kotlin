package com.acme.transport.customs.clearance.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualClearanceManifestTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualClearanceManifest("code-477", "name-793")
        assertEquals(ManualClearanceManifestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newManualClearanceManifest("code-477", "name-793")
        try {
            item.moveTo(ManualClearanceManifestStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualClearanceManifestStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualClearanceManifest("code-477", "name-793")
        item.status = ManualClearanceManifestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualClearanceManifestStatus.PROPOSED) }
    }
}
