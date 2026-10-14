package com.acme.transport.terminal.crossdock.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockRuleTest {
    @Test
    fun `starts in draft`() {
        val item = newCrossDockRule("code-793", "name-475")
        assertEquals(CrossDockRuleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newCrossDockRule("code-793", "name-475")
        try {
            item.moveTo(CrossDockRuleStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockRuleStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCrossDockRule("code-793", "name-475")
        item.status = CrossDockRuleStatus.CLOSED
        assertFails("move") { item.moveTo(CrossDockRuleStatus.DRAFT) }
    }
}
