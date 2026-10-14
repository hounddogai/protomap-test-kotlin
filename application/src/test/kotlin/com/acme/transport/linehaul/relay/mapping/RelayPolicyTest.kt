package com.acme.transport.linehaul.relay.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayPolicyTest {
    @Test
    fun `starts in open`() {
        val item = newRelayPolicy("code-991", "label-473")
        assertEquals(RelayPolicyStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newRelayPolicy("code-991", "label-473")
        try {
            item.moveTo(RelayPolicyStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayPolicyStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRelayPolicy("code-991", "label-473")
        item.status = RelayPolicyStatus.ARCHIVED
        assertFails("move") { item.moveTo(RelayPolicyStatus.OPEN) }
    }
}
