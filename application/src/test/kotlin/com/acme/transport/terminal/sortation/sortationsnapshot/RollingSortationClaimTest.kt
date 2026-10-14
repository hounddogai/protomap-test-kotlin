package com.acme.transport.terminal.sortation.sortationsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingSortationClaimTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingSortationClaim("code-660", "label-444")
        assertEquals(RollingSortationClaimStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRollingSortationClaim("code-660", "label-444")
        try {
            item.moveTo(RollingSortationClaimStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingSortationClaimStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingSortationClaim("code-660", "label-444")
        item.status = RollingSortationClaimStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingSortationClaimStatus.REQUESTED) }
    }
}
