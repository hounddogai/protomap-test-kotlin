package com.acme.transport.terminal.dockdoor.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDockDoorClaimTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientDockDoorClaim("code-996", "name-428")
        assertEquals(LenientDockDoorClaimStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newLenientDockDoorClaim("code-996", "name-428")
        try {
            item.moveTo(LenientDockDoorClaimStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDockDoorClaimStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientDockDoorClaim("code-996", "name-428")
        item.status = LenientDockDoorClaimStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientDockDoorClaimStatus.PLANNED) }
    }
}
