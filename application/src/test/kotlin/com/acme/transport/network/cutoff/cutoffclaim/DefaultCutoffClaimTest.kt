package com.acme.transport.network.cutoff.cutoffclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultCutoffClaimTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultCutoffClaim("code-71", "name-232")
        assertEquals(DefaultCutoffClaimStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newDefaultCutoffClaim("code-71", "name-232")
        try {
            item.moveTo(DefaultCutoffClaimStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultCutoffClaimStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultCutoffClaim("code-71", "name-232")
        item.status = DefaultCutoffClaimStatus.FAILED
        assertFails("move") { item.moveTo(DefaultCutoffClaimStatus.NEW) }
    }
}
