package com.acme.transport.freightpay.audit.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalAuditAllocationTest {
    @Test
    fun `starts in proposed`() {
        val item = newRegionalAuditAllocation("code-620", "title-272")
        assertEquals(RegionalAuditAllocationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRegionalAuditAllocation("code-620", "title-272")
        try {
            item.moveTo(RegionalAuditAllocationStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalAuditAllocationStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRegionalAuditAllocation("code-620", "title-272")
        item.status = RegionalAuditAllocationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RegionalAuditAllocationStatus.PROPOSED) }
    }
}
