package com.acme.transport.safety.scorecard.defaultscorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardAdjustmentTest {
    @Test
    fun `starts in created`() {
        val item = newScorecardAdjustment("code-846", "title-496")
        assertEquals(ScorecardAdjustmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newScorecardAdjustment("code-846", "title-496")
        try {
            item.moveTo(ScorecardAdjustmentStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardAdjustmentStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScorecardAdjustment("code-846", "title-496")
        item.status = ScorecardAdjustmentStatus.RETIRED
        assertFails("move") { item.moveTo(ScorecardAdjustmentStatus.CREATED) }
    }
}
