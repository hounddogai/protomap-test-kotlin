package com.acme.transport.freightpay.audit.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditVarianceTest {
    @Test
    fun `starts in draft`() {
        val item = newAuditVariance("code-116", "title-518")
        assertEquals(AuditVarianceStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAuditVariance("code-116", "title-518")
        try {
            item.moveTo(AuditVarianceStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditVarianceStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAuditVariance("code-116", "title-518")
        item.status = AuditVarianceStatus.CLOSED
        assertFails("move") { item.moveTo(AuditVarianceStatus.DRAFT) }
    }
}
