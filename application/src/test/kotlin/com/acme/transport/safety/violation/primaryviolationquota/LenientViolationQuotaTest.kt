package com.acme.transport.safety.violation.primaryviolationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientViolationQuotaTest {
    @Test
    fun `starts in new`() {
        val item = newLenientViolationQuota("code-748", "title-20")
        assertEquals(LenientViolationQuotaStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLenientViolationQuota("code-748", "title-20")
        try {
            item.moveTo(LenientViolationQuotaStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientViolationQuotaStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientViolationQuota("code-748", "title-20")
        item.status = LenientViolationQuotaStatus.FAILED
        assertFails("move") { item.moveTo(LenientViolationQuotaStatus.NEW) }
    }
}
