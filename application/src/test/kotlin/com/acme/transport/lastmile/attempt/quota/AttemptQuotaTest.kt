package com.acme.transport.lastmile.attempt.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptQuotaTest {
    @Test
    fun `starts in idle`() {
        val item = newAttemptQuota("code-144", "title-932")
        assertEquals(AttemptQuotaStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newAttemptQuota("code-144", "title-932")
        try {
            item.moveTo(AttemptQuotaStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptQuotaStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAttemptQuota("code-144", "title-932")
        item.status = AttemptQuotaStatus.STOPPED
        assertFails("move") { item.moveTo(AttemptQuotaStatus.IDLE) }
    }
}
