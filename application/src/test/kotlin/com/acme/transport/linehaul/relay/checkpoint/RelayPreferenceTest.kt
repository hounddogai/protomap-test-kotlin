package com.acme.transport.linehaul.relay.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayPreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newRelayPreference("code-118", "label-61")
        assertEquals(RelayPreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRelayPreference("code-118", "label-61")
        try {
            item.moveTo(RelayPreferenceStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayPreferenceStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRelayPreference("code-118", "label-61")
        item.status = RelayPreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(RelayPreferenceStatus.REQUESTED) }
    }
}
