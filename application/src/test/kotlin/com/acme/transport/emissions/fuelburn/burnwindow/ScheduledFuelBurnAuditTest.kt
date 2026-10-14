package com.acme.transport.emissions.fuelburn.burnwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledFuelBurnAuditTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledFuelBurnAudit("code-424", "label-81")
        assertEquals(ScheduledFuelBurnAuditStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newScheduledFuelBurnAudit("code-424", "label-81")
        try {
            item.moveTo(ScheduledFuelBurnAuditStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledFuelBurnAuditStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledFuelBurnAudit("code-424", "label-81")
        item.status = ScheduledFuelBurnAuditStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledFuelBurnAuditStatus.CREATED) }
    }
}
