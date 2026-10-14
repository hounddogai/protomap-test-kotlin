package com.acme.transport.safety.violation.manualviolationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictViolationQuotaTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictViolationQuota("code-265", "title-774")
        assertEquals(StrictViolationQuotaStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newStrictViolationQuota("code-265", "title-774")
        try {
            item.moveTo(StrictViolationQuotaStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictViolationQuotaStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictViolationQuota("code-265", "title-774")
        item.status = StrictViolationQuotaStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictViolationQuotaStatus.PLANNED) }
    }
}
