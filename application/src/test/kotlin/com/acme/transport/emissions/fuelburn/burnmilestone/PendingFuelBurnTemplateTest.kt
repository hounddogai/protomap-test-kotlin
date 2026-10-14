package com.acme.transport.emissions.fuelburn.burnmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingFuelBurnTemplateTest {
    @Test
    fun `starts in open`() {
        val item = newPendingFuelBurnTemplate("code-584", "title-232")
        assertEquals(PendingFuelBurnTemplateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newPendingFuelBurnTemplate("code-584", "title-232")
        try {
            item.moveTo(PendingFuelBurnTemplateStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingFuelBurnTemplateStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPendingFuelBurnTemplate("code-584", "title-232")
        item.status = PendingFuelBurnTemplateStatus.ARCHIVED
        assertFails("move") { item.moveTo(PendingFuelBurnTemplateStatus.OPEN) }
    }
}
