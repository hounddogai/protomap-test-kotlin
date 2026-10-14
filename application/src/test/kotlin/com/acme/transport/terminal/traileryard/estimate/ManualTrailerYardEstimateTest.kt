package com.acme.transport.terminal.traileryard.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTrailerYardEstimateTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualTrailerYardEstimate("code-684", "name-550")
        assertEquals(ManualTrailerYardEstimateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newManualTrailerYardEstimate("code-684", "name-550")
        try {
            item.moveTo(ManualTrailerYardEstimateStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTrailerYardEstimateStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualTrailerYardEstimate("code-684", "name-550")
        item.status = ManualTrailerYardEstimateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualTrailerYardEstimateStatus.PROPOSED) }
    }
}
