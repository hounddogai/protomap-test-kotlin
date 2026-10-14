package com.acme.transport.freightpay.audit.legacyauditpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveAuditTemplateTest {
    @Test
    fun `starts in proposed`() {
        val item = newAdaptiveAuditTemplate("code-1", "title-974")
        assertEquals(AdaptiveAuditTemplateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newAdaptiveAuditTemplate("code-1", "title-974")
        try {
            item.moveTo(AdaptiveAuditTemplateStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveAuditTemplateStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAdaptiveAuditTemplate("code-1", "title-974")
        item.status = AdaptiveAuditTemplateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AdaptiveAuditTemplateStatus.PROPOSED) }
    }
}
