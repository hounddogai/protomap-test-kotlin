package com.acme.transport.lastmile.attempt.attemptprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalAttemptClaimTest {
    @Test
    fun `starts in new`() {
        val item = newGlobalAttemptClaim("code-980", "label-495")
        assertEquals(GlobalAttemptClaimStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newGlobalAttemptClaim("code-980", "label-495")
        try {
            item.moveTo(GlobalAttemptClaimStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalAttemptClaimStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newGlobalAttemptClaim("code-980", "label-495")
        item.status = GlobalAttemptClaimStatus.FAILED
        assertFails("move") { item.moveTo(GlobalAttemptClaimStatus.NEW) }
    }
}
