package com.acme.transport.crewing.dutyperiod.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodGroupTest {
    @Test
    fun `starts in idle`() {
        val item = newDutyPeriodGroup("code-983", "title-636")
        assertEquals(DutyPeriodGroupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDutyPeriodGroup("code-983", "title-636")
        try {
            item.moveTo(DutyPeriodGroupStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodGroupStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDutyPeriodGroup("code-983", "title-636")
        item.status = DutyPeriodGroupStatus.STOPPED
        assertFails("move") { item.moveTo(DutyPeriodGroupStatus.IDLE) }
    }
}
