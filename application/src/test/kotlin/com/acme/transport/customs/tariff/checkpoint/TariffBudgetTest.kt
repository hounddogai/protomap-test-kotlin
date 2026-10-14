package com.acme.transport.customs.tariff.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffBudgetTest {
    @Test
    fun `starts in new`() {
        val item = newTariffBudget("code-499", "name-705")
        assertEquals(TariffBudgetStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newTariffBudget("code-499", "name-705")
        try {
            item.moveTo(TariffBudgetStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffBudgetStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTariffBudget("code-499", "name-705")
        item.status = TariffBudgetStatus.FAILED
        assertFails("move") { item.moveTo(TariffBudgetStatus.NEW) }
    }
}
