package com.acme.transport.freightpay.audit.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditMetricTest {
    @Test
    fun `starts in created`() {
        val item = newAuditMetric("code-82", "title-601")
        assertEquals(AuditMetricStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAuditMetric("code-82", "title-601")
        try {
            item.moveTo(AuditMetricStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditMetricStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAuditMetric("code-82", "title-601")
        item.status = AuditMetricStatus.RETIRED
        assertFails("move") { item.moveTo(AuditMetricStatus.CREATED) }
    }
}
