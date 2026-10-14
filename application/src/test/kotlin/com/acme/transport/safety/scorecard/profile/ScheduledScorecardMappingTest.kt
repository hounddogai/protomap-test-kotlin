package com.acme.transport.safety.scorecard.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledScorecardMappingTest {
    @Test
    fun `starts in proposed`() {
        val item = newScheduledScorecardMapping("code-416", "name-6")
        assertEquals(ScheduledScorecardMappingStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newScheduledScorecardMapping("code-416", "name-6")
        try {
            item.moveTo(ScheduledScorecardMappingStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledScorecardMappingStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScheduledScorecardMapping("code-416", "name-6")
        item.status = ScheduledScorecardMappingStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScheduledScorecardMappingStatus.PROPOSED) }
    }
}
