package com.acme.transport.lastmile.attempt.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualAttemptQuotaTest {
    @Test
    fun `starts in draft`() {
        val item = newManualAttemptQuota("code-574", "title-543")
        assertEquals(ManualAttemptQuotaStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newManualAttemptQuota("code-574", "title-543")
        try {
            item.moveTo(ManualAttemptQuotaStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualAttemptQuotaStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualAttemptQuota("code-574", "title-543")
        item.status = ManualAttemptQuotaStatus.CLOSED
        assertFails("move") { item.moveTo(ManualAttemptQuotaStatus.DRAFT) }
    }
}
