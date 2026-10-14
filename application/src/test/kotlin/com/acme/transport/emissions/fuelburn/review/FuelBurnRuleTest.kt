package com.acme.transport.emissions.fuelburn.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnRuleTest {
    @Test
    fun `starts in planned`() {
        val item = newFuelBurnRule("code-380", "name-298")
        assertEquals(FuelBurnRuleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newFuelBurnRule("code-380", "name-298")
        try {
            item.moveTo(FuelBurnRuleStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnRuleStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFuelBurnRule("code-380", "name-298")
        item.status = FuelBurnRuleStatus.CANCELLED
        assertFails("move") { item.moveTo(FuelBurnRuleStatus.PLANNED) }
    }
}
