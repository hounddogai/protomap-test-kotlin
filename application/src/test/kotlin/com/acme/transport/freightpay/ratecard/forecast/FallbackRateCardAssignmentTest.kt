package com.acme.transport.freightpay.ratecard.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRateCardAssignmentTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackRateCardAssignment("code-427", "name-42")
        assertEquals(FallbackRateCardAssignmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newFallbackRateCardAssignment("code-427", "name-42")
        try {
            item.moveTo(FallbackRateCardAssignmentStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRateCardAssignmentStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackRateCardAssignment("code-427", "name-42")
        item.status = FallbackRateCardAssignmentStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackRateCardAssignmentStatus.CREATED) }
    }
}
