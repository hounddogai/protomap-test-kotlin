package com.acme.transport.crewing.dutyperiod.periodnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDutyPeriodNoticeTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualDutyPeriodNotice("code-147", "label-252")
        assertEquals(ManualDutyPeriodNoticeStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newManualDutyPeriodNotice("code-147", "label-252")
        try {
            item.moveTo(ManualDutyPeriodNoticeStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDutyPeriodNoticeStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualDutyPeriodNotice("code-147", "label-252")
        item.status = ManualDutyPeriodNoticeStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualDutyPeriodNoticeStatus.PROPOSED) }
    }
}
