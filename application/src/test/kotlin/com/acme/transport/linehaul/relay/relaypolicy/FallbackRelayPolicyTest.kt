package com.acme.transport.linehaul.relay.relaypolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRelayPolicyTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackRelayPolicy("code-662", "title-672")
        assertEquals(FallbackRelayPolicyStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newFallbackRelayPolicy("code-662", "title-672")
        try {
            item.moveTo(FallbackRelayPolicyStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRelayPolicyStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackRelayPolicy("code-662", "title-672")
        item.status = FallbackRelayPolicyStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackRelayPolicyStatus.OPEN) }
    }
}
