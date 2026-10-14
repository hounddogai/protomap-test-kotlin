package com.acme.transport.emissions.emissionfactor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorSlotTest {
    @Test
    fun `starts in new`() {
        val item = newEmissionFactorSlot("code-609", "title-942")
        assertEquals(EmissionFactorSlotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newEmissionFactorSlot("code-609", "title-942")
        try {
            item.moveTo(EmissionFactorSlotStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorSlotStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newEmissionFactorSlot("code-609", "title-942")
        item.status = EmissionFactorSlotStatus.FAILED
        assertFails("move") { item.moveTo(EmissionFactorSlotStatus.NEW) }
    }
}
