package com.acme.transport.lastmile.drop.dropreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropRuleTest {
    @Test
    fun `starts in requested`() {
        val item = newDropRule("code-210", "title-33")
        assertEquals(DropRuleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDropRule("code-210", "title-33")
        try {
            item.moveTo(DropRuleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropRuleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDropRule("code-210", "title-33")
        item.status = DropRuleStatus.EXPIRED
        assertFails("move") { item.moveTo(DropRuleStatus.REQUESTED) }
    }
}
