package com.acme.transport.safety.violation.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedViolationRequestTest {
    @Test
    fun `starts in created`() {
        val item = newWeightedViolationRequest("code-860", "label-281")
        assertEquals(WeightedViolationRequestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newWeightedViolationRequest("code-860", "label-281")
        try {
            item.moveTo(WeightedViolationRequestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedViolationRequestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newWeightedViolationRequest("code-860", "label-281")
        item.status = WeightedViolationRequestStatus.RETIRED
        assertFails("move") { item.moveTo(WeightedViolationRequestStatus.CREATED) }
    }
}
