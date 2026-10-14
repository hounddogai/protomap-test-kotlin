package com.acme.transport.emissions.emissionfactor.factorschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultEmissionFactorAdjustmentTest {
    @Test
    fun `starts in draft`() {
        val item = newDefaultEmissionFactorAdjustment("code-873", "name-977")
        assertEquals(DefaultEmissionFactorAdjustmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newDefaultEmissionFactorAdjustment("code-873", "name-977")
        try {
            item.moveTo(DefaultEmissionFactorAdjustmentStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultEmissionFactorAdjustmentStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefaultEmissionFactorAdjustment("code-873", "name-977")
        item.status = DefaultEmissionFactorAdjustmentStatus.CLOSED
        assertFails("move") { item.moveTo(DefaultEmissionFactorAdjustmentStatus.DRAFT) }
    }
}
