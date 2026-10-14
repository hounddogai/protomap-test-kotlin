package com.acme.transport.crewing.restbreak.breakvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedRestBreakRollupTest {
    @Test
    fun `starts in idle`() {
        val item = newWeightedRestBreakRollup("code-342", "label-446")
        assertEquals(WeightedRestBreakRollupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newWeightedRestBreakRollup("code-342", "label-446")
        try {
            item.moveTo(WeightedRestBreakRollupStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedRestBreakRollupStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newWeightedRestBreakRollup("code-342", "label-446")
        item.status = WeightedRestBreakRollupStatus.STOPPED
        assertFails("move") { item.moveTo(WeightedRestBreakRollupStatus.IDLE) }
    }
}
