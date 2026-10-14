package com.acme.transport.safety.scorecard.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardWindowTest {
    @Test
    fun `starts in open`() {
        val item = newScorecardWindow("code-549", "title-280")
        assertEquals(ScorecardWindowStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newScorecardWindow("code-549", "title-280")
        try {
            item.moveTo(ScorecardWindowStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardWindowStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScorecardWindow("code-549", "title-280")
        item.status = ScorecardWindowStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScorecardWindowStatus.OPEN) }
    }
}
