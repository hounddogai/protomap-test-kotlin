package com.acme.transport.lastmile.attempt.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualAttemptBaselineTest {
    @Test
    fun `starts in draft`() {
        val item = newManualAttemptBaseline("code-895", "title-900")
        assertEquals(ManualAttemptBaselineStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newManualAttemptBaseline("code-895", "title-900")
        try {
            item.moveTo(ManualAttemptBaselineStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualAttemptBaselineStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualAttemptBaseline("code-895", "title-900")
        item.status = ManualAttemptBaselineStatus.CLOSED
        assertFails("move") { item.moveTo(ManualAttemptBaselineStatus.DRAFT) }
    }
}
