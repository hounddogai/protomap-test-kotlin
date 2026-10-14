package com.acme.transport.safety.violation.violationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalViolationQuotaTest {
    @Test
    fun `starts in created`() {
        val item = newSeasonalViolationQuota("code-112", "label-363")
        assertEquals(SeasonalViolationQuotaStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newSeasonalViolationQuota("code-112", "label-363")
        try {
            item.moveTo(SeasonalViolationQuotaStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalViolationQuotaStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSeasonalViolationQuota("code-112", "label-363")
        item.status = SeasonalViolationQuotaStatus.RETIRED
        assertFails("move") { item.moveTo(SeasonalViolationQuotaStatus.CREATED) }
    }
}
