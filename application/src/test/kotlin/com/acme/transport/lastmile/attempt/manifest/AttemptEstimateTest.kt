package com.acme.transport.lastmile.attempt.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptEstimateTest {
    @Test
    fun `starts in new`() {
        val item = newAttemptEstimate("code-292", "name-551")
        assertEquals(AttemptEstimateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newAttemptEstimate("code-292", "name-551")
        try {
            item.moveTo(AttemptEstimateStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptEstimateStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAttemptEstimate("code-292", "name-551")
        item.status = AttemptEstimateStatus.FAILED
        assertFails("move") { item.moveTo(AttemptEstimateStatus.NEW) }
    }
}
