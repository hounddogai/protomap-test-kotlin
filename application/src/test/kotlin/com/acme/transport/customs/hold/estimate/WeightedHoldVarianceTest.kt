package com.acme.transport.customs.hold.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedHoldVarianceTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedHoldVariance("code-919", "label-843")
        assertEquals(WeightedHoldVarianceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newWeightedHoldVariance("code-919", "label-843")
        try {
            item.moveTo(WeightedHoldVarianceStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedHoldVarianceStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedHoldVariance("code-919", "label-843")
        item.status = WeightedHoldVarianceStatus.FAILED
        assertFails("move") { item.moveTo(WeightedHoldVarianceStatus.NEW) }
    }
}
