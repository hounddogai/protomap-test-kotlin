package com.acme.transport.safety.scorecard.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardBundleTest {
    @Test
    fun `starts in open`() {
        val item = newScorecardBundle("code-288", "label-205")
        assertEquals(ScorecardBundleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newScorecardBundle("code-288", "label-205")
        try {
            item.moveTo(ScorecardBundleStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardBundleStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScorecardBundle("code-288", "label-205")
        item.status = ScorecardBundleStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScorecardBundleStatus.OPEN) }
    }
}
