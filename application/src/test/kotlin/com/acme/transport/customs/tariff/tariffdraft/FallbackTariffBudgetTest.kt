package com.acme.transport.customs.tariff.tariffdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackTariffBudgetTest {
    @Test
    fun `starts in planned`() {
        val item = newFallbackTariffBudget("code-299", "label-783")
        assertEquals(FallbackTariffBudgetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newFallbackTariffBudget("code-299", "label-783")
        try {
            item.moveTo(FallbackTariffBudgetStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackTariffBudgetStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFallbackTariffBudget("code-299", "label-783")
        item.status = FallbackTariffBudgetStatus.CANCELLED
        assertFails("move") { item.moveTo(FallbackTariffBudgetStatus.PLANNED) }
    }
}
