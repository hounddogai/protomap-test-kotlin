package com.acme.transport.fleetops.inspection.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionNoticeTest {
    @Test
    fun `starts in new`() {
        val item = newInspectionNotice("code-661", "name-105")
        assertEquals(InspectionNoticeStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newInspectionNotice("code-661", "name-105")
        try {
            item.moveTo(InspectionNoticeStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionNoticeStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newInspectionNotice("code-661", "name-105")
        item.status = InspectionNoticeStatus.FAILED
        assertFails("move") { item.moveTo(InspectionNoticeStatus.NEW) }
    }
}
