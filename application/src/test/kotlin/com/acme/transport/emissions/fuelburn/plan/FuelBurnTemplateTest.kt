package com.acme.transport.emissions.fuelburn.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnTemplateTest {
    @Test
    fun `starts in open`() {
        val item = newFuelBurnTemplate("code-307", "name-701")
        assertEquals(FuelBurnTemplateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newFuelBurnTemplate("code-307", "name-701")
        try {
            item.moveTo(FuelBurnTemplateStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnTemplateStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFuelBurnTemplate("code-307", "name-701")
        item.status = FuelBurnTemplateStatus.ARCHIVED
        assertFails("move") { item.moveTo(FuelBurnTemplateStatus.OPEN) }
    }
}
