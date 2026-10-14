package com.acme.transport.customs.tariff.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffMilestoneTest {
    @Test
    fun `starts in idle`() {
        val item = newTariffMilestone("code-428", "name-498")
        assertEquals(TariffMilestoneStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newTariffMilestone("code-428", "name-498")
        try {
            item.moveTo(TariffMilestoneStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffMilestoneStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTariffMilestone("code-428", "name-498")
        item.status = TariffMilestoneStatus.STOPPED
        assertFails("move") { item.moveTo(TariffMilestoneStatus.IDLE) }
    }
}
