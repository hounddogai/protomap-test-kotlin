package com.acme.transport.customs.clearance.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyClearanceScoreTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyClearanceScore("code-304", "label-205")
        assertEquals(LegacyClearanceScoreStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLegacyClearanceScore("code-304", "label-205")
        try {
            item.moveTo(LegacyClearanceScoreStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyClearanceScoreStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyClearanceScore("code-304", "label-205")
        item.status = LegacyClearanceScoreStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyClearanceScoreStatus.PROPOSED) }
    }
}
