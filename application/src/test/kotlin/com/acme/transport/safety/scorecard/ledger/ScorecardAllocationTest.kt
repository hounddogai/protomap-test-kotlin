package com.acme.transport.safety.scorecard.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardAllocationTest {
    @Test
    fun `starts in proposed`() {
        val item = newScorecardAllocation("code-775", "title-557")
        assertEquals(ScorecardAllocationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newScorecardAllocation("code-775", "title-557")
        try {
            item.moveTo(ScorecardAllocationStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardAllocationStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScorecardAllocation("code-775", "title-557")
        item.status = ScorecardAllocationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScorecardAllocationStatus.PROPOSED) }
    }
}
