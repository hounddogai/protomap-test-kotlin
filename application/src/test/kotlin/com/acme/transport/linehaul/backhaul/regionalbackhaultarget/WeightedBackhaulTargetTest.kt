package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedBackhaulTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedBackhaulTarget("code-726", "name-161")
        assertEquals(WeightedBackhaulTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newWeightedBackhaulTarget("code-726", "name-161")
        try {
            item.moveTo(WeightedBackhaulTargetStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedBackhaulTargetStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedBackhaulTarget("code-726", "name-161")
        item.status = WeightedBackhaulTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedBackhaulTargetStatus.PLANNED) }
    }
}
