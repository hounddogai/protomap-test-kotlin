package com.acme.transport.terminal.crossdock.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveCrossDockRuleTest {
    @Test
    fun `starts in open`() {
        val item = newAdaptiveCrossDockRule("code-38", "name-624")
        assertEquals(AdaptiveCrossDockRuleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAdaptiveCrossDockRule("code-38", "name-624")
        try {
            item.moveTo(AdaptiveCrossDockRuleStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveCrossDockRuleStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAdaptiveCrossDockRule("code-38", "name-624")
        item.status = AdaptiveCrossDockRuleStatus.ARCHIVED
        assertFails("move") { item.moveTo(AdaptiveCrossDockRuleStatus.OPEN) }
    }
}
