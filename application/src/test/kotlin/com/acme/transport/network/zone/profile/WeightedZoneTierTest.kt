package com.acme.transport.network.zone.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedZoneTierTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedZoneTier("code-452", "label-845")
        assertEquals(WeightedZoneTierStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newWeightedZoneTier("code-452", "label-845")
        try {
            item.moveTo(WeightedZoneTierStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedZoneTierStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedZoneTier("code-452", "label-845")
        item.status = WeightedZoneTierStatus.FAILED
        assertFails("move") { item.moveTo(WeightedZoneTierStatus.NEW) }
    }
}
