package com.acme.transport.crewing.dutyperiod.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodRecordTest {
    @Test
    fun `starts in planned`() {
        val item = newDutyPeriodRecord("code-704", "name-204")
        assertEquals(DutyPeriodRecordStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newDutyPeriodRecord("code-704", "name-204")
        try {
            item.moveTo(DutyPeriodRecordStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodRecordStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDutyPeriodRecord("code-704", "name-204")
        item.status = DutyPeriodRecordStatus.CANCELLED
        assertFails("move") { item.moveTo(DutyPeriodRecordStatus.PLANNED) }
    }
}
