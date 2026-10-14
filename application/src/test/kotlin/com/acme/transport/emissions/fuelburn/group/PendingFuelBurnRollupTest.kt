package com.acme.transport.emissions.fuelburn.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingFuelBurnRollupTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingFuelBurnRollup("code-276", "label-955")
        assertEquals(PendingFuelBurnRollupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPendingFuelBurnRollup("code-276", "label-955")
        try {
            item.moveTo(PendingFuelBurnRollupStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingFuelBurnRollupStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingFuelBurnRollup("code-276", "label-955")
        item.status = PendingFuelBurnRollupStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingFuelBurnRollupStatus.PLANNED) }
    }
}
