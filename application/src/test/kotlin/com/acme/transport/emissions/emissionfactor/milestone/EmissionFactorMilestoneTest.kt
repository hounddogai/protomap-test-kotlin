package com.acme.transport.emissions.emissionfactor.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorMilestoneTest {
    @Test
    fun `starts in draft`() {
        val item = newEmissionFactorMilestone("code-778", "label-208")
        assertEquals(EmissionFactorMilestoneStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newEmissionFactorMilestone("code-778", "label-208")
        try {
            item.moveTo(EmissionFactorMilestoneStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorMilestoneStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newEmissionFactorMilestone("code-778", "label-208")
        item.status = EmissionFactorMilestoneStatus.CLOSED
        assertFails("move") { item.moveTo(EmissionFactorMilestoneStatus.DRAFT) }
    }
}
