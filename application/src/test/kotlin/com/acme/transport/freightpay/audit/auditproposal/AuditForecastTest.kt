package com.acme.transport.freightpay.audit.auditproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditForecastTest {
    @Test
    fun `starts in new`() {
        val item = newAuditForecast("code-307", "label-238")
        assertEquals(AuditForecastStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newAuditForecast("code-307", "label-238")
        try {
            item.moveTo(AuditForecastStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditForecastStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAuditForecast("code-307", "label-238")
        item.status = AuditForecastStatus.FAILED
        assertFails("move") { item.moveTo(AuditForecastStatus.NEW) }
    }
}
