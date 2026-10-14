package com.acme.transport.linehaul.laneplan.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledLanePlanRecordTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledLanePlanRecord("code-882", "label-956")
        assertEquals(ScheduledLanePlanRecordStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newScheduledLanePlanRecord("code-882", "label-956")
        try {
            item.moveTo(ScheduledLanePlanRecordStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledLanePlanRecordStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledLanePlanRecord("code-882", "label-956")
        item.status = ScheduledLanePlanRecordStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledLanePlanRecordStatus.OPEN) }
    }
}
