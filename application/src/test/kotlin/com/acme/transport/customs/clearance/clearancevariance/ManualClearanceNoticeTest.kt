package com.acme.transport.customs.clearance.clearancevariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualClearanceNoticeTest {
    @Test
    fun `starts in idle`() {
        val item = newManualClearanceNotice("code-239", "label-891")
        assertEquals(ManualClearanceNoticeStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newManualClearanceNotice("code-239", "label-891")
        try {
            item.moveTo(ManualClearanceNoticeStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualClearanceNoticeStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualClearanceNotice("code-239", "label-891")
        item.status = ManualClearanceNoticeStatus.STOPPED
        assertFails("move") { item.moveTo(ManualClearanceNoticeStatus.IDLE) }
    }
}
