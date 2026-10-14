package com.acme.transport.freightpay.audit.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditTemplateTest {
    @Test
    fun `starts in proposed`() {
        val item = newAuditTemplate("code-647", "label-734")
        assertEquals(AuditTemplateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newAuditTemplate("code-647", "label-734")
        try {
            item.moveTo(AuditTemplateStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditTemplateStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAuditTemplate("code-647", "label-734")
        item.status = AuditTemplateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AuditTemplateStatus.PROPOSED) }
    }
}
