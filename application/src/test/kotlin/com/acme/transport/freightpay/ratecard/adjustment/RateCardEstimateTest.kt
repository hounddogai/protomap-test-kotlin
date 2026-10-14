package com.acme.transport.freightpay.ratecard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardEstimateTest {
    @Test
    fun `starts in created`() {
        val item = newRateCardEstimate("code-584", "name-532")
        assertEquals(RateCardEstimateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newRateCardEstimate("code-584", "name-532")
        try {
            item.moveTo(RateCardEstimateStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardEstimateStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRateCardEstimate("code-584", "name-532")
        item.status = RateCardEstimateStatus.RETIRED
        assertFails("move") { item.moveTo(RateCardEstimateStatus.CREATED) }
    }
}
