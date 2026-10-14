package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRateCardAssignmentTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalRateCardAssignment("code-92", "name-42")
        assertEquals(GlobalRateCardAssignmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newGlobalRateCardAssignment("code-92", "name-42")
        try {
            item.moveTo(GlobalRateCardAssignmentStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRateCardAssignmentStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalRateCardAssignment("code-92", "name-42")
        item.status = GlobalRateCardAssignmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalRateCardAssignmentStatus.OPEN) }
    }
}
