package com.acme.transport.terminal.traileryard.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingTrailerYardRuleTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingTrailerYardRule("code-583", "title-870")
        assertEquals(RollingTrailerYardRuleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRollingTrailerYardRule("code-583", "title-870")
        try {
            item.moveTo(RollingTrailerYardRuleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingTrailerYardRuleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingTrailerYardRule("code-583", "title-870")
        item.status = RollingTrailerYardRuleStatus.STOPPED
        assertFails("move") { item.moveTo(RollingTrailerYardRuleStatus.IDLE) }
    }
}
