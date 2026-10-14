package com.acme.transport.customs.hold.holdmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledHoldMappingTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledHoldMapping("code-508", "label-178")
        assertEquals(ScheduledHoldMappingStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newScheduledHoldMapping("code-508", "label-178")
        try {
            item.moveTo(ScheduledHoldMappingStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledHoldMappingStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledHoldMapping("code-508", "label-178")
        item.status = ScheduledHoldMappingStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledHoldMappingStatus.OPEN) }
    }
}
