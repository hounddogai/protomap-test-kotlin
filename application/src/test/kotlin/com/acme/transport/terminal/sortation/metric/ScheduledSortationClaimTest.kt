package com.acme.transport.terminal.sortation.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledSortationClaimTest {
    @Test
    fun `starts in requested`() {
        val item = newScheduledSortationClaim("code-19", "label-198")
        assertEquals(ScheduledSortationClaimStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newScheduledSortationClaim("code-19", "label-198")
        try {
            item.moveTo(ScheduledSortationClaimStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledSortationClaimStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScheduledSortationClaim("code-19", "label-198")
        item.status = ScheduledSortationClaimStatus.EXPIRED
        assertFails("move") { item.moveTo(ScheduledSortationClaimStatus.REQUESTED) }
    }
}
