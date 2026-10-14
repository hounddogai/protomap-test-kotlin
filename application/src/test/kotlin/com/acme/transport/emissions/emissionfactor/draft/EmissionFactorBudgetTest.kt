package com.acme.transport.emissions.emissionfactor.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorBudgetTest {
    @Test
    fun `starts in planned`() {
        val item = newEmissionFactorBudget("code-340", "name-671")
        assertEquals(EmissionFactorBudgetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newEmissionFactorBudget("code-340", "name-671")
        try {
            item.moveTo(EmissionFactorBudgetStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorBudgetStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newEmissionFactorBudget("code-340", "name-671")
        item.status = EmissionFactorBudgetStatus.CANCELLED
        assertFails("move") { item.moveTo(EmissionFactorBudgetStatus.PLANNED) }
    }
}
