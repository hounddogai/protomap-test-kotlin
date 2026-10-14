package com.acme.transport.freightpay.audit.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditEstimateTest {
    @Test
    fun `starts in proposed`() {
        val item = newAuditEstimate("code-221", "title-639")
        assertEquals(AuditEstimateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAuditEstimate("code-221", "title-639")
        try {
            item.moveTo(AuditEstimateStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditEstimateStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAuditEstimate("code-221", "title-639")
        item.status = AuditEstimateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AuditEstimateStatus.PROPOSED) }
    }
}
