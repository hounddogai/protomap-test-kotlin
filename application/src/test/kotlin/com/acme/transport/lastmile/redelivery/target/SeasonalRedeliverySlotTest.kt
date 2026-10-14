package com.acme.transport.lastmile.redelivery.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRedeliverySlotTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalRedeliverySlot("code-179", "title-241")
        assertEquals(SeasonalRedeliverySlotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newSeasonalRedeliverySlot("code-179", "title-241")
        try {
            item.moveTo(SeasonalRedeliverySlotStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRedeliverySlotStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalRedeliverySlot("code-179", "title-241")
        item.status = SeasonalRedeliverySlotStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalRedeliverySlotStatus.IDLE) }
    }
}
