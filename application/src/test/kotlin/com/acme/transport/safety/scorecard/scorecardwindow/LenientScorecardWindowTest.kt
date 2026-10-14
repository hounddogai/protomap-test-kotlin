package com.acme.transport.safety.scorecard.scorecardwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientScorecardWindowTest {
    @Test
    fun `starts in open`() {
        val item = newLenientScorecardWindow("code-81", "title-319")
        assertEquals(LenientScorecardWindowStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newLenientScorecardWindow("code-81", "title-319")
        try {
            item.moveTo(LenientScorecardWindowStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientScorecardWindowStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientScorecardWindow("code-81", "title-319")
        item.status = LenientScorecardWindowStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientScorecardWindowStatus.OPEN) }
    }
}
