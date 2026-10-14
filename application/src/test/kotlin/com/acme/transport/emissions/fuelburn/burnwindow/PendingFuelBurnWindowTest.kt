package com.acme.transport.emissions.fuelburn.burnwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingFuelBurnWindowTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingFuelBurnWindow("code-385", "title-388")
        assertEquals(PendingFuelBurnWindowStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newPendingFuelBurnWindow("code-385", "title-388")
        try {
            item.moveTo(PendingFuelBurnWindowStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingFuelBurnWindowStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingFuelBurnWindow("code-385", "title-388")
        item.status = PendingFuelBurnWindowStatus.CLOSED
        assertFails("move") { item.moveTo(PendingFuelBurnWindowStatus.DRAFT) }
    }
}
