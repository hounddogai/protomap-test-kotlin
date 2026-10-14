package com.acme.transport.customs.tariff.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveTariffMilestoneTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveTariffMilestone("code-175", "label-539")
        assertEquals(AdaptiveTariffMilestoneStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newAdaptiveTariffMilestone("code-175", "label-539")
        try {
            item.moveTo(AdaptiveTariffMilestoneStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveTariffMilestoneStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveTariffMilestone("code-175", "label-539")
        item.status = AdaptiveTariffMilestoneStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveTariffMilestoneStatus.DRAFT) }
    }
}
