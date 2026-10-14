package com.acme.transport.crewing.availability.availabilityscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedAvailabilityGroupTest {
    @Test
    fun `starts in idle`() {
        val item = newWeightedAvailabilityGroup("code-996", "name-82")
        assertEquals(WeightedAvailabilityGroupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newWeightedAvailabilityGroup("code-996", "name-82")
        try {
            item.moveTo(WeightedAvailabilityGroupStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedAvailabilityGroupStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newWeightedAvailabilityGroup("code-996", "name-82")
        item.status = WeightedAvailabilityGroupStatus.STOPPED
        assertFails("move") { item.moveTo(WeightedAvailabilityGroupStatus.IDLE) }
    }
}
