package com.acme.transport.crewing.restbreak.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledRestBreakLedgerTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledRestBreakLedger("code-263", "label-392")
        assertEquals(ScheduledRestBreakLedgerStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newScheduledRestBreakLedger("code-263", "label-392")
        try {
            item.moveTo(ScheduledRestBreakLedgerStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledRestBreakLedgerStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledRestBreakLedger("code-263", "label-392")
        item.status = ScheduledRestBreakLedgerStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledRestBreakLedgerStatus.OPEN) }
    }
}
