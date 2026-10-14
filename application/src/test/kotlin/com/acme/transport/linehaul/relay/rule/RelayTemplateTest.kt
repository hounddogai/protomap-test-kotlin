package com.acme.transport.linehaul.relay.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayTemplateTest {
    @Test
    fun `starts in draft`() {
        val item = newRelayTemplate("code-79", "label-326")
        assertEquals(RelayTemplateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRelayTemplate("code-79", "label-326")
        try {
            item.moveTo(RelayTemplateStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayTemplateStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRelayTemplate("code-79", "label-326")
        item.status = RelayTemplateStatus.CLOSED
        assertFails("move") { item.moveTo(RelayTemplateStatus.DRAFT) }
    }
}
