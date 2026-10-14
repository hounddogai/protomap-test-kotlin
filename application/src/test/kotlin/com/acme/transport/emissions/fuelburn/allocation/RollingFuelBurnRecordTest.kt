package com.acme.transport.emissions.fuelburn.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingFuelBurnRecordTest {
    @Test
    fun `starts in open`() {
        val item = newRollingFuelBurnRecord("code-572", "name-844")
        assertEquals(RollingFuelBurnRecordStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newRollingFuelBurnRecord("code-572", "name-844")
        try {
            item.moveTo(RollingFuelBurnRecordStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingFuelBurnRecordStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingFuelBurnRecord("code-572", "name-844")
        item.status = RollingFuelBurnRecordStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingFuelBurnRecordStatus.OPEN) }
    }
}
