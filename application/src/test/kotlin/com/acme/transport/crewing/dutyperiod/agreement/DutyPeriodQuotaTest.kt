package com.acme.transport.crewing.dutyperiod.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodQuotaTest {
    @Test
    fun `starts in created`() {
        val item = newDutyPeriodQuota("code-542", "title-460")
        assertEquals(DutyPeriodQuotaStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDutyPeriodQuota("code-542", "title-460")
        try {
            item.moveTo(DutyPeriodQuotaStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodQuotaStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDutyPeriodQuota("code-542", "title-460")
        item.status = DutyPeriodQuotaStatus.RETIRED
        assertFails("move") { item.moveTo(DutyPeriodQuotaStatus.CREATED) }
    }
}
