package com.acme.transport.linehaul.capacity.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityQuotaTest {
    @Test
    fun `starts in proposed`() {
        val item = newCapacityQuota("code-125", "title-313")
        assertEquals(CapacityQuotaStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCapacityQuota("code-125", "title-313")
        try {
            item.moveTo(CapacityQuotaStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityQuotaStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCapacityQuota("code-125", "title-313")
        item.status = CapacityQuotaStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CapacityQuotaStatus.PROPOSED) }
    }
}
