package com.acme.transport.lastmile.attempt.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualAttemptEstimateTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualAttemptEstimate("code-438", "label-997")
        assertEquals(ManualAttemptEstimateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newManualAttemptEstimate("code-438", "label-997")
        try {
            item.moveTo(ManualAttemptEstimateStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualAttemptEstimateStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualAttemptEstimate("code-438", "label-997")
        item.status = ManualAttemptEstimateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualAttemptEstimateStatus.PROPOSED) }
    }
}
