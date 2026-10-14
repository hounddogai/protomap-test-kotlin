package com.acme.transport.crewing.swap.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedSwapBaselineTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedSwapBaseline("code-392", "name-546")
        assertEquals(WeightedSwapBaselineStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newWeightedSwapBaseline("code-392", "name-546")
        try {
            item.moveTo(WeightedSwapBaselineStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedSwapBaselineStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedSwapBaseline("code-392", "name-546")
        item.status = WeightedSwapBaselineStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedSwapBaselineStatus.PLANNED) }
    }
}
