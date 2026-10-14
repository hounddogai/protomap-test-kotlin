package com.acme.transport.linehaul.tender.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderScheduleTest {
    @Test
    fun `starts in proposed`() {
        val item = newTenderSchedule("code-533", "label-924")
        assertEquals(TenderScheduleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTenderSchedule("code-533", "label-924")
        try {
            item.moveTo(TenderScheduleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderScheduleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTenderSchedule("code-533", "label-924")
        item.status = TenderScheduleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TenderScheduleStatus.PROPOSED) }
    }
}
