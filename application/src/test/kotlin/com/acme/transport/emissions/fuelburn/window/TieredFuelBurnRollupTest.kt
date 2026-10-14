package com.acme.transport.emissions.fuelburn.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredFuelBurnRollupTest {
    @Test
    fun `starts in requested`() {
        val item = newTieredFuelBurnRollup("code-261", "label-807")
        assertEquals(TieredFuelBurnRollupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTieredFuelBurnRollup("code-261", "label-807")
        try {
            item.moveTo(TieredFuelBurnRollupStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredFuelBurnRollupStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTieredFuelBurnRollup("code-261", "label-807")
        item.status = TieredFuelBurnRollupStatus.EXPIRED
        assertFails("move") { item.moveTo(TieredFuelBurnRollupStatus.REQUESTED) }
    }
}
