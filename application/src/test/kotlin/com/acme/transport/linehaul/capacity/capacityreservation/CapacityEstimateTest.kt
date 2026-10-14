package com.acme.transport.linehaul.capacity.capacityreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityEstimateTest {
    @Test
    fun `starts in created`() {
        val item = newCapacityEstimate("code-42", "title-456")
        assertEquals(CapacityEstimateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCapacityEstimate("code-42", "title-456")
        try {
            item.moveTo(CapacityEstimateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityEstimateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCapacityEstimate("code-42", "title-456")
        item.status = CapacityEstimateStatus.RETIRED
        assertFails("move") { item.moveTo(CapacityEstimateStatus.CREATED) }
    }
}
