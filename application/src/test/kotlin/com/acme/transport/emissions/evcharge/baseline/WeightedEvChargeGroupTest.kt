package com.acme.transport.emissions.evcharge.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedEvChargeGroupTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedEvChargeGroup("code-630", "title-901")
        assertEquals(WeightedEvChargeGroupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newWeightedEvChargeGroup("code-630", "title-901")
        try {
            item.moveTo(WeightedEvChargeGroupStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedEvChargeGroupStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedEvChargeGroup("code-630", "title-901")
        item.status = WeightedEvChargeGroupStatus.FAILED
        assertFails("move") { item.moveTo(WeightedEvChargeGroupStatus.NEW) }
    }
}
