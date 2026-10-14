package com.acme.transport.freightpay.ratecard.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedRateCardThresholdTest {
    @Test
    fun `starts in proposed`() {
        val item = newWeightedRateCardThreshold("code-631", "name-918")
        assertEquals(WeightedRateCardThresholdStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newWeightedRateCardThreshold("code-631", "name-918")
        try {
            item.moveTo(WeightedRateCardThresholdStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedRateCardThresholdStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newWeightedRateCardThreshold("code-631", "name-918")
        item.status = WeightedRateCardThresholdStatus.WITHDRAWN
        assertFails("move") { item.moveTo(WeightedRateCardThresholdStatus.PROPOSED) }
    }
}
