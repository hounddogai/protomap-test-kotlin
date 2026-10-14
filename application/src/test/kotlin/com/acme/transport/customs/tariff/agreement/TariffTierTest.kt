package com.acme.transport.customs.tariff.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffTierTest {
    @Test
    fun `starts in planned`() {
        val item = newTariffTier("code-114", "title-101")
        assertEquals(TariffTierStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newTariffTier("code-114", "title-101")
        try {
            item.moveTo(TariffTierStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffTierStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTariffTier("code-114", "title-101")
        item.status = TariffTierStatus.CANCELLED
        assertFails("move") { item.moveTo(TariffTierStatus.PLANNED) }
    }
}
