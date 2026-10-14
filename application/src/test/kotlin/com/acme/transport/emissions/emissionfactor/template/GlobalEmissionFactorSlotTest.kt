package com.acme.transport.emissions.emissionfactor.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalEmissionFactorSlotTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalEmissionFactorSlot("code-41", "name-713")
        assertEquals(GlobalEmissionFactorSlotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newGlobalEmissionFactorSlot("code-41", "name-713")
        try {
            item.moveTo(GlobalEmissionFactorSlotStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalEmissionFactorSlotStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalEmissionFactorSlot("code-41", "name-713")
        item.status = GlobalEmissionFactorSlotStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalEmissionFactorSlotStatus.PLANNED) }
    }
}
