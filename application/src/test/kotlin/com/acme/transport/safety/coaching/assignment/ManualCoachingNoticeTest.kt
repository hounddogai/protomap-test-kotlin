package com.acme.transport.safety.coaching.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCoachingNoticeTest {
    @Test
    fun `starts in idle`() {
        val item = newManualCoachingNotice("code-289", "title-76")
        assertEquals(ManualCoachingNoticeStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newManualCoachingNotice("code-289", "title-76")
        try {
            item.moveTo(ManualCoachingNoticeStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCoachingNoticeStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualCoachingNotice("code-289", "title-76")
        item.status = ManualCoachingNoticeStatus.STOPPED
        assertFails("move") { item.moveTo(ManualCoachingNoticeStatus.IDLE) }
    }
}
