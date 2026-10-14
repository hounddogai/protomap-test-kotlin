package com.acme.transport.safety.scorecard.defaultscorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedScorecardProfileTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedScorecardProfile("code-234", "label-427")
        assertEquals(WeightedScorecardProfileStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newWeightedScorecardProfile("code-234", "label-427")
        try {
            item.moveTo(WeightedScorecardProfileStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedScorecardProfileStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedScorecardProfile("code-234", "label-427")
        item.status = WeightedScorecardProfileStatus.FAILED
        assertFails("move") { item.moveTo(WeightedScorecardProfileStatus.NEW) }
    }
}
