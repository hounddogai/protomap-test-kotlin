package com.acme.transport.emissions.fuelburn.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnAuditTest {
    @Test
    fun `starts in draft`() {
        val item = newFuelBurnAudit("code-809", "label-939")
        assertEquals(FuelBurnAuditStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newFuelBurnAudit("code-809", "label-939")
        try {
            item.moveTo(FuelBurnAuditStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnAuditStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFuelBurnAudit("code-809", "label-939")
        item.status = FuelBurnAuditStatus.CLOSED
        assertFails("move") { item.moveTo(FuelBurnAuditStatus.DRAFT) }
    }
}
