package com.acme.transport.safety.scorecard.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackScorecardTargetTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackScorecardTarget("code-875", "name-251")
        assertEquals(FallbackScorecardTargetStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newFallbackScorecardTarget("code-875", "name-251")
        try {
            item.moveTo(FallbackScorecardTargetStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackScorecardTargetStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackScorecardTarget("code-875", "name-251")
        item.status = FallbackScorecardTargetStatus.FAILED
        assertFails("move") { item.moveTo(FallbackScorecardTargetStatus.NEW) }
    }
}
