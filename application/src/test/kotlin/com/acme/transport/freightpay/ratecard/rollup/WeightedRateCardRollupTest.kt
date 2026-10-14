package com.acme.transport.freightpay.ratecard.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedRateCardRollupTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedRateCardRollup("code-687", "name-240")
        assertEquals(WeightedRateCardRollupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newWeightedRateCardRollup("code-687", "name-240")
        try {
            item.moveTo(WeightedRateCardRollupStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedRateCardRollupStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedRateCardRollup("code-687", "name-240")
        item.status = WeightedRateCardRollupStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedRateCardRollupStatus.PLANNED) }
    }
}
