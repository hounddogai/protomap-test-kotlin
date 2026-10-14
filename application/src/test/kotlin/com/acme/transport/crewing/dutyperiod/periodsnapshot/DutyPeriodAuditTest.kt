package com.acme.transport.crewing.dutyperiod.periodsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodAuditTest {
    @Test
    fun `starts in proposed`() {
        val item = newDutyPeriodAudit("code-172", "name-587")
        assertEquals(DutyPeriodAuditStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newDutyPeriodAudit("code-172", "name-587")
        try {
            item.moveTo(DutyPeriodAuditStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodAuditStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDutyPeriodAudit("code-172", "name-587")
        item.status = DutyPeriodAuditStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DutyPeriodAuditStatus.PROPOSED) }
    }
}
