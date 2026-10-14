package com.acme.transport.terminal.sortation.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredSortationRuleTest {
    @Test
    fun `starts in planned`() {
        val item = newTieredSortationRule("code-963", "title-936")
        assertEquals(TieredSortationRuleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newTieredSortationRule("code-963", "title-936")
        try {
            item.moveTo(TieredSortationRuleStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredSortationRuleStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTieredSortationRule("code-963", "title-936")
        item.status = TieredSortationRuleStatus.CANCELLED
        assertFails("move") { item.moveTo(TieredSortationRuleStatus.PLANNED) }
    }
}
