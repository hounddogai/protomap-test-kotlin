package com.acme.transport.crewing.restbreak.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakRuleTest {
    @Test
    fun `starts in idle`() {
        val item = newRestBreakRule("code-47", "name-760")
        assertEquals(RestBreakRuleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRestBreakRule("code-47", "name-760")
        try {
            item.moveTo(RestBreakRuleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakRuleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRestBreakRule("code-47", "name-760")
        item.status = RestBreakRuleStatus.STOPPED
        assertFails("move") { item.moveTo(RestBreakRuleStatus.IDLE) }
    }
}
