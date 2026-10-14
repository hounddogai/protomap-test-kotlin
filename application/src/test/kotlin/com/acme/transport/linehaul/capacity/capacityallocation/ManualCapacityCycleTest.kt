package com.acme.transport.linehaul.capacity.capacityallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCapacityCycleTest {
    @Test
    fun `starts in created`() {
        val item = newManualCapacityCycle("code-455", "title-331")
        assertEquals(ManualCapacityCycleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newManualCapacityCycle("code-455", "title-331")
        try {
            item.moveTo(ManualCapacityCycleStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCapacityCycleStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualCapacityCycle("code-455", "title-331")
        item.status = ManualCapacityCycleStatus.RETIRED
        assertFails("move") { item.moveTo(ManualCapacityCycleStatus.CREATED) }
    }
}
