package com.acme.transport.safety.scorecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryScorecardTargetTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryScorecardTarget("code-105", "label-659")
        assertEquals(PrimaryScorecardTargetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newPrimaryScorecardTarget("code-105", "label-659")
        try {
            item.moveTo(PrimaryScorecardTargetStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryScorecardTargetStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryScorecardTarget("code-105", "label-659")
        item.status = PrimaryScorecardTargetStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryScorecardTargetStatus.CREATED) }
    }
}
