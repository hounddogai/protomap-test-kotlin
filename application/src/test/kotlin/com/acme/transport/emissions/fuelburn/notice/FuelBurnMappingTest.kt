package com.acme.transport.emissions.fuelburn.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnMappingTest {
    @Test
    fun `starts in new`() {
        val item = newFuelBurnMapping("code-234", "title-853")
        assertEquals(FuelBurnMappingStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newFuelBurnMapping("code-234", "title-853")
        try {
            item.moveTo(FuelBurnMappingStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnMappingStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFuelBurnMapping("code-234", "title-853")
        item.status = FuelBurnMappingStatus.FAILED
        assertFails("move") { item.moveTo(FuelBurnMappingStatus.NEW) }
    }
}
