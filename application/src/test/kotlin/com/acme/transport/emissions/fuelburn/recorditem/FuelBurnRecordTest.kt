package com.acme.transport.emissions.fuelburn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnRecordTest {
    @Test
    fun `starts in created`() {
        val item = newFuelBurnRecord("code-610", "title-727")
        assertEquals(FuelBurnRecordStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newFuelBurnRecord("code-610", "title-727")
        try {
            item.moveTo(FuelBurnRecordStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnRecordStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFuelBurnRecord("code-610", "title-727")
        item.status = FuelBurnRecordStatus.RETIRED
        assertFails("move") { item.moveTo(FuelBurnRecordStatus.CREATED) }
    }
}
