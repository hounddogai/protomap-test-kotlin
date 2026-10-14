package com.acme.transport.safety.scorecard.scorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultScorecardAllocationTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultScorecardAllocation("code-153", "title-802")
        assertEquals(DefaultScorecardAllocationStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDefaultScorecardAllocation("code-153", "title-802")
        try {
            item.moveTo(DefaultScorecardAllocationStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultScorecardAllocationStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultScorecardAllocation("code-153", "title-802")
        item.status = DefaultScorecardAllocationStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultScorecardAllocationStatus.CREATED) }
    }
}
