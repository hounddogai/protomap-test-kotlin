package com.acme.transport.emissions.emissionfactor.factortarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveEmissionFactorMilestoneTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveEmissionFactorMilestone("code-274", "title-743")
        assertEquals(AdaptiveEmissionFactorMilestoneStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newAdaptiveEmissionFactorMilestone("code-274", "title-743")
        try {
            item.moveTo(AdaptiveEmissionFactorMilestoneStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveEmissionFactorMilestoneStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveEmissionFactorMilestone("code-274", "title-743")
        item.status = AdaptiveEmissionFactorMilestoneStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveEmissionFactorMilestoneStatus.DRAFT) }
    }
}
