package com.acme.transport.emissions.emissionfactor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorAssignmentTest {
    @Test
    fun `starts in requested`() {
        val item = newEmissionFactorAssignment("code-245", "name-936")
        assertEquals(EmissionFactorAssignmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newEmissionFactorAssignment("code-245", "name-936")
        try {
            item.moveTo(EmissionFactorAssignmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorAssignmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newEmissionFactorAssignment("code-245", "name-936")
        item.status = EmissionFactorAssignmentStatus.EXPIRED
        assertFails("move") { item.moveTo(EmissionFactorAssignmentStatus.REQUESTED) }
    }
}
