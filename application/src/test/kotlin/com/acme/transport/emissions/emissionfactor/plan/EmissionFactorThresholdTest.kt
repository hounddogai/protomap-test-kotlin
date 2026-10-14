package com.acme.transport.emissions.emissionfactor.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorThresholdTest {
    @Test
    fun `starts in draft`() {
        val item = newEmissionFactorThreshold("code-218", "label-298")
        assertEquals(EmissionFactorThresholdStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newEmissionFactorThreshold("code-218", "label-298")
        try {
            item.moveTo(EmissionFactorThresholdStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorThresholdStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newEmissionFactorThreshold("code-218", "label-298")
        item.status = EmissionFactorThresholdStatus.CLOSED
        assertFails("move") { item.moveTo(EmissionFactorThresholdStatus.DRAFT) }
    }
}
