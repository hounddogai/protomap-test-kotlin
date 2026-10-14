package com.acme.transport.safety.violation.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationQuotaTest {
    @Test
    fun `starts in idle`() {
        val item = newViolationQuota("code-291", "title-889")
        assertEquals(ViolationQuotaStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newViolationQuota("code-291", "title-889")
        try {
            item.moveTo(ViolationQuotaStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationQuotaStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newViolationQuota("code-291", "title-889")
        item.status = ViolationQuotaStatus.STOPPED
        assertFails("move") { item.moveTo(ViolationQuotaStatus.IDLE) }
    }
}
