package com.acme.transport.safety.scorecard.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardMappingTest {
    @Test
    fun `starts in proposed`() {
        val item = newScorecardMapping("code-846", "label-27")
        assertEquals(ScorecardMappingStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newScorecardMapping("code-846", "label-27")
        try {
            item.moveTo(ScorecardMappingStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardMappingStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScorecardMapping("code-846", "label-27")
        item.status = ScorecardMappingStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScorecardMappingStatus.PROPOSED) }
    }
}
