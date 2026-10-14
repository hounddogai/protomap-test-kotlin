package com.acme.transport.freightpay.accessorial.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedAccessorialVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedAccessorialVariance("code-774", "title-736")
        assertEquals(WeightedAccessorialVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newWeightedAccessorialVariance("code-774", "title-736")
        try {
            item.moveTo(WeightedAccessorialVarianceStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedAccessorialVarianceStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedAccessorialVariance("code-774", "title-736")
        item.status = WeightedAccessorialVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedAccessorialVarianceStatus.PLANNED) }
    }
}
