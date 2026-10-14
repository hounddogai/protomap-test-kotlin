package com.acme.transport.customs.tariff.tariffprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyTariffAllocationTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyTariffAllocation("code-366", "name-803")
        assertEquals(LegacyTariffAllocationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLegacyTariffAllocation("code-366", "name-803")
        try {
            item.moveTo(LegacyTariffAllocationStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyTariffAllocationStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyTariffAllocation("code-366", "name-803")
        item.status = LegacyTariffAllocationStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyTariffAllocationStatus.PLANNED) }
    }
}
