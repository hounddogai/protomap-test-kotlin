package com.acme.transport.linehaul.trailer.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerRuleTest {
    @Test
    fun `starts in requested`() {
        val item = newTrailerRule("code-827", "label-513")
        assertEquals(TrailerRuleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTrailerRule("code-827", "label-513")
        try {
            item.moveTo(TrailerRuleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerRuleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTrailerRule("code-827", "label-513")
        item.status = TrailerRuleStatus.EXPIRED
        assertFails("move") { item.moveTo(TrailerRuleStatus.REQUESTED) }
    }
}
