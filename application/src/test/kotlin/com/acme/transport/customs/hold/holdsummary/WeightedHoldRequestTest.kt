package com.acme.transport.customs.hold.holdsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedHoldRequestTest {
    @Test
    fun `starts in proposed`() {
        val item = newWeightedHoldRequest("code-753", "name-990")
        assertEquals(WeightedHoldRequestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newWeightedHoldRequest("code-753", "name-990")
        try {
            item.moveTo(WeightedHoldRequestStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedHoldRequestStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newWeightedHoldRequest("code-753", "name-990")
        item.status = WeightedHoldRequestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(WeightedHoldRequestStatus.PROPOSED) }
    }
}
