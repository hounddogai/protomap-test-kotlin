package com.acme.transport.emissions.emissionfactor.factorsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedEmissionFactorBatchTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedEmissionFactorBatch("code-935", "name-451")
        assertEquals(WeightedEmissionFactorBatchStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newWeightedEmissionFactorBatch("code-935", "name-451")
        try {
            item.moveTo(WeightedEmissionFactorBatchStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedEmissionFactorBatchStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedEmissionFactorBatch("code-935", "name-451")
        item.status = WeightedEmissionFactorBatchStatus.FAILED
        assertFails("move") { item.moveTo(WeightedEmissionFactorBatchStatus.NEW) }
    }
}
