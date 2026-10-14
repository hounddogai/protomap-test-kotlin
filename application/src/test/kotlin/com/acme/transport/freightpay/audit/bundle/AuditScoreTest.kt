package com.acme.transport.freightpay.audit.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditScoreTest {
    @Test
    fun `starts in new`() {
        val item = newAuditScore("code-228", "label-694")
        assertEquals(AuditScoreStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newAuditScore("code-228", "label-694")
        try {
            item.moveTo(AuditScoreStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditScoreStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAuditScore("code-228", "label-694")
        item.status = AuditScoreStatus.FAILED
        assertFails("move") { item.moveTo(AuditScoreStatus.NEW) }
    }
}
