package com.acme.transport.emissions.fuelburn.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnSegmentTest {
    @Test
    fun `starts in open`() {
        val item = newFuelBurnSegment("code-472", "label-864")
        assertEquals(FuelBurnSegmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newFuelBurnSegment("code-472", "label-864")
        try {
            item.moveTo(FuelBurnSegmentStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnSegmentStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFuelBurnSegment("code-472", "label-864")
        item.status = FuelBurnSegmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(FuelBurnSegmentStatus.OPEN) }
    }
}
