package com.acme.transport.emissions.fuelburn.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnSnapshotTest {
    @Test
    fun `starts in draft`() {
        val item = newFuelBurnSnapshot("code-130", "name-416")
        assertEquals(FuelBurnSnapshotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newFuelBurnSnapshot("code-130", "name-416")
        try {
            item.moveTo(FuelBurnSnapshotStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnSnapshotStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFuelBurnSnapshot("code-130", "name-416")
        item.status = FuelBurnSnapshotStatus.CLOSED
        assertFails("move") { item.moveTo(FuelBurnSnapshotStatus.DRAFT) }
    }
}
