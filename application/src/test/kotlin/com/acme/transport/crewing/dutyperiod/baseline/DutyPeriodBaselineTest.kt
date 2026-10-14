package com.acme.transport.crewing.dutyperiod.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodBaselineTest {
    @Test
    fun `starts in open`() {
        val item = newDutyPeriodBaseline("code-146", "title-342")
        assertEquals(DutyPeriodBaselineStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newDutyPeriodBaseline("code-146", "title-342")
        try {
            item.moveTo(DutyPeriodBaselineStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodBaselineStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDutyPeriodBaseline("code-146", "title-342")
        item.status = DutyPeriodBaselineStatus.ARCHIVED
        assertFails("move") { item.moveTo(DutyPeriodBaselineStatus.OPEN) }
    }
}
