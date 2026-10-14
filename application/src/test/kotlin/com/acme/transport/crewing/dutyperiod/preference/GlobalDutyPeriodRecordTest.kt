package com.acme.transport.crewing.dutyperiod.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalDutyPeriodRecordTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalDutyPeriodRecord("code-712", "title-934")
        assertEquals(GlobalDutyPeriodRecordStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newGlobalDutyPeriodRecord("code-712", "title-934")
        try {
            item.moveTo(GlobalDutyPeriodRecordStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalDutyPeriodRecordStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalDutyPeriodRecord("code-712", "title-934")
        item.status = GlobalDutyPeriodRecordStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalDutyPeriodRecordStatus.CREATED) }
    }
}
