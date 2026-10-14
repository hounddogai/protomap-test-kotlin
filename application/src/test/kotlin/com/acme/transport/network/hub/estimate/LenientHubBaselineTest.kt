package com.acme.transport.network.hub.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientHubBaselineTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientHubBaseline("code-415", "name-315")
        assertEquals(LenientHubBaselineStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newLenientHubBaseline("code-415", "name-315")
        try {
            item.moveTo(LenientHubBaselineStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientHubBaselineStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientHubBaseline("code-415", "name-315")
        item.status = LenientHubBaselineStatus.CLOSED
        assertFails("move") { item.moveTo(LenientHubBaselineStatus.DRAFT) }
    }
}
