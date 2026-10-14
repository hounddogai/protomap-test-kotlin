package com.acme.transport.safety.scorecard.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardThresholdTest {
    @Test
    fun `starts in open`() {
        val item = newScorecardThreshold("code-447", "title-426")
        assertEquals(ScorecardThresholdStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newScorecardThreshold("code-447", "title-426")
        try {
            item.moveTo(ScorecardThresholdStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardThresholdStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScorecardThreshold("code-447", "title-426")
        item.status = ScorecardThresholdStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScorecardThresholdStatus.OPEN) }
    }
}
