package com.acme.transport.terminal.traileryard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictTrailerYardEstimateTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictTrailerYardEstimate("code-642", "name-354")
        assertEquals(StrictTrailerYardEstimateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newStrictTrailerYardEstimate("code-642", "name-354")
        try {
            item.moveTo(StrictTrailerYardEstimateStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictTrailerYardEstimateStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictTrailerYardEstimate("code-642", "name-354")
        item.status = StrictTrailerYardEstimateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictTrailerYardEstimateStatus.PROPOSED) }
    }
}
