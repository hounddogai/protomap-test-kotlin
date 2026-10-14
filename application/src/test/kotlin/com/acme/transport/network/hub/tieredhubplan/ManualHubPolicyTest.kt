package com.acme.transport.network.hub.tieredhubplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualHubPolicyTest {
    @Test
    fun `starts in draft`() {
        val item = newManualHubPolicy("code-912", "name-606")
        assertEquals(ManualHubPolicyStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newManualHubPolicy("code-912", "name-606")
        try {
            item.moveTo(ManualHubPolicyStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualHubPolicyStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualHubPolicy("code-912", "name-606")
        item.status = ManualHubPolicyStatus.CLOSED
        assertFails("move") { item.moveTo(ManualHubPolicyStatus.DRAFT) }
    }
}
