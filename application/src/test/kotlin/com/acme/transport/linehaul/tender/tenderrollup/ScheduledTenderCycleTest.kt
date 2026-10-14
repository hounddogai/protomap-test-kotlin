package com.acme.transport.linehaul.tender.tenderrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledTenderCycleTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledTenderCycle("code-409", "title-439")
        assertEquals(ScheduledTenderCycleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newScheduledTenderCycle("code-409", "title-439")
        try {
            item.moveTo(ScheduledTenderCycleStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledTenderCycleStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledTenderCycle("code-409", "title-439")
        item.status = ScheduledTenderCycleStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledTenderCycleStatus.OPEN) }
    }
}
