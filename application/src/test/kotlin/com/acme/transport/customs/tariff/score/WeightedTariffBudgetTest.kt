package com.acme.transport.customs.tariff.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTariffBudgetTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedTariffBudget("code-282", "title-449")
        assertEquals(WeightedTariffBudgetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newWeightedTariffBudget("code-282", "title-449")
        try {
            item.moveTo(WeightedTariffBudgetStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTariffBudgetStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedTariffBudget("code-282", "title-449")
        item.status = WeightedTariffBudgetStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedTariffBudgetStatus.OPEN) }
    }
}
