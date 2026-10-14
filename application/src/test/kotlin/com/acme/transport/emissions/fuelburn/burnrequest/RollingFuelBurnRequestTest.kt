package com.acme.transport.emissions.fuelburn.burnrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingFuelBurnRequestTest {
    @Test
    fun `starts in open`() {
        val item = newRollingFuelBurnRequest("code-696", "label-270")
        assertEquals(RollingFuelBurnRequestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRollingFuelBurnRequest("code-696", "label-270")
        try {
            item.moveTo(RollingFuelBurnRequestStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingFuelBurnRequestStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingFuelBurnRequest("code-696", "label-270")
        item.status = RollingFuelBurnRequestStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingFuelBurnRequestStatus.OPEN) }
    }
}
