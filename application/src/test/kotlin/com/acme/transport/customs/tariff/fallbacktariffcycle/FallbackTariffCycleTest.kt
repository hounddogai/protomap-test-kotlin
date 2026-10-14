package com.acme.transport.customs.tariff.fallbacktariffcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackTariffCycleTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackTariffCycle("code-939", "title-357")
        assertEquals(FallbackTariffCycleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newFallbackTariffCycle("code-939", "title-357")
        try {
            item.moveTo(FallbackTariffCycleStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackTariffCycleStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackTariffCycle("code-939", "title-357")
        item.status = FallbackTariffCycleStatus.FAILED
        assertFails("move") { item.moveTo(FallbackTariffCycleStatus.NEW) }
    }
}
