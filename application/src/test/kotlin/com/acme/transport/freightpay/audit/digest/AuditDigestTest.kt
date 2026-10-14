package com.acme.transport.freightpay.audit.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditDigestTest {
    @Test
    fun `starts in planned`() {
        val item = newAuditDigest("code-938", "label-795")
        assertEquals(AuditDigestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newAuditDigest("code-938", "label-795")
        try {
            item.moveTo(AuditDigestStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditDigestStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAuditDigest("code-938", "label-795")
        item.status = AuditDigestStatus.CANCELLED
        assertFails("move") { item.moveTo(AuditDigestStatus.PLANNED) }
    }
}
