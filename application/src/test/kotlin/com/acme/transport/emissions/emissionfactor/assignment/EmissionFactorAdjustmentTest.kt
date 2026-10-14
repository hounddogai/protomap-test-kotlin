package com.acme.transport.emissions.emissionfactor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorAdjustmentTest {
    @Test
    fun `starts in requested`() {
        val item = newEmissionFactorAdjustment("code-707", "label-818")
        assertEquals(EmissionFactorAdjustmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newEmissionFactorAdjustment("code-707", "label-818")
        try {
            item.moveTo(EmissionFactorAdjustmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorAdjustmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newEmissionFactorAdjustment("code-707", "label-818")
        item.status = EmissionFactorAdjustmentStatus.EXPIRED
        assertFails("move") { item.moveTo(EmissionFactorAdjustmentStatus.REQUESTED) }
    }
}
