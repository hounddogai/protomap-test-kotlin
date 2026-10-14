package com.acme.transport.safety.scorecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryScorecardWindowTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryScorecardWindow("code-568", "label-428")
        assertEquals(PrimaryScorecardWindowStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newPrimaryScorecardWindow("code-568", "label-428")
        try {
            item.moveTo(PrimaryScorecardWindowStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryScorecardWindowStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryScorecardWindow("code-568", "label-428")
        item.status = PrimaryScorecardWindowStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryScorecardWindowStatus.OPEN) }
    }
}
