package com.acme.transport.linehaul.backhaul.backhaulthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulRuleTest {
    @Test
    fun `starts in planned`() {
        val item = newBackhaulRule("code-313", "name-931")
        assertEquals(BackhaulRuleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newBackhaulRule("code-313", "name-931")
        try {
            item.moveTo(BackhaulRuleStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulRuleStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBackhaulRule("code-313", "name-931")
        item.status = BackhaulRuleStatus.CANCELLED
        assertFails("move") { item.moveTo(BackhaulRuleStatus.PLANNED) }
    }
}
