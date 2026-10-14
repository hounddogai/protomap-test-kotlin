package com.acme.transport.freightpay.audit.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveAuditEstimateTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveAuditEstimate("code-558", "title-723")
        assertEquals(AdaptiveAuditEstimateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newAdaptiveAuditEstimate("code-558", "title-723")
        try {
            item.moveTo(AdaptiveAuditEstimateStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveAuditEstimateStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveAuditEstimate("code-558", "title-723")
        item.status = AdaptiveAuditEstimateStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveAuditEstimateStatus.REQUESTED) }
    }
}
