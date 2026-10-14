package com.acme.transport.lastmile.attempt.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyAttemptAuditTest {
    @Test
    fun `starts in created`() {
        val item = newLegacyAttemptAudit("code-332", "title-691")
        assertEquals(LegacyAttemptAuditStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLegacyAttemptAudit("code-332", "title-691")
        try {
            item.moveTo(LegacyAttemptAuditStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyAttemptAuditStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLegacyAttemptAudit("code-332", "title-691")
        item.status = LegacyAttemptAuditStatus.RETIRED
        assertFails("move") { item.moveTo(LegacyAttemptAuditStatus.CREATED) }
    }
}
