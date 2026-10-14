package com.acme.transport.safety.violation.violationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationNoticeTest {
    @Test
    fun `starts in planned`() {
        val item = newViolationNotice("code-515", "label-873")
        assertEquals(ViolationNoticeStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newViolationNotice("code-515", "label-873")
        try {
            item.moveTo(ViolationNoticeStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationNoticeStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newViolationNotice("code-515", "label-873")
        item.status = ViolationNoticeStatus.CANCELLED
        assertFails("move") { item.moveTo(ViolationNoticeStatus.PLANNED) }
    }
}
