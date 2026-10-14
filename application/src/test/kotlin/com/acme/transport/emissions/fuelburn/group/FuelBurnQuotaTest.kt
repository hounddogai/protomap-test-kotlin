package com.acme.transport.emissions.fuelburn.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnQuotaTest {
    @Test
    fun `starts in new`() {
        val item = newFuelBurnQuota("code-563", "title-446")
        assertEquals(FuelBurnQuotaStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newFuelBurnQuota("code-563", "title-446")
        try {
            item.moveTo(FuelBurnQuotaStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnQuotaStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFuelBurnQuota("code-563", "title-446")
        item.status = FuelBurnQuotaStatus.FAILED
        assertFails("move") { item.moveTo(FuelBurnQuotaStatus.NEW) }
    }
}
