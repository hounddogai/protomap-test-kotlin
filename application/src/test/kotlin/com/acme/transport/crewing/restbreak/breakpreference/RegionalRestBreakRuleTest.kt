package com.acme.transport.crewing.restbreak.breakpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalRestBreakRuleTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalRestBreakRule("code-587", "label-103")
        assertEquals(RegionalRestBreakRuleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newRegionalRestBreakRule("code-587", "label-103")
        try {
            item.moveTo(RegionalRestBreakRuleStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalRestBreakRuleStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalRestBreakRule("code-587", "label-103")
        item.status = RegionalRestBreakRuleStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalRestBreakRuleStatus.REQUESTED) }
    }
}
