package com.acme.transport.crewing.dutyperiod.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodMappingTest {
    @Test
    fun `starts in new`() {
        val item = newDutyPeriodMapping("code-698", "name-458")
        assertEquals(DutyPeriodMappingStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newDutyPeriodMapping("code-698", "name-458")
        try {
            item.moveTo(DutyPeriodMappingStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodMappingStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDutyPeriodMapping("code-698", "name-458")
        item.status = DutyPeriodMappingStatus.FAILED
        assertFails("move") { item.moveTo(DutyPeriodMappingStatus.NEW) }
    }
}
