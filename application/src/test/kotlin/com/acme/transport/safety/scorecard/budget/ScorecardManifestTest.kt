package com.acme.transport.safety.scorecard.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardManifestTest {
    @Test
    fun `starts in new`() {
        val item = newScorecardManifest("code-777", "title-618")
        assertEquals(ScorecardManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newScorecardManifest("code-777", "title-618")
        try {
            item.moveTo(ScorecardManifestStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardManifestStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScorecardManifest("code-777", "title-618")
        item.status = ScorecardManifestStatus.FAILED
        assertFails("move") { item.moveTo(ScorecardManifestStatus.NEW) }
    }
}
