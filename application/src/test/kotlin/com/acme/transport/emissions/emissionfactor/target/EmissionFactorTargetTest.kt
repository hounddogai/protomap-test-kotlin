package com.acme.transport.emissions.emissionfactor.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newEmissionFactorTarget("code-198", "title-581")
        assertEquals(EmissionFactorTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newEmissionFactorTarget("code-198", "title-581")
        try {
            item.moveTo(EmissionFactorTargetStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorTargetStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newEmissionFactorTarget("code-198", "title-581")
        item.status = EmissionFactorTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(EmissionFactorTargetStatus.PLANNED) }
    }
}
