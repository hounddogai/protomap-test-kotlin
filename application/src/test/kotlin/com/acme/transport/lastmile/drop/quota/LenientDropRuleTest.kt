package com.acme.transport.lastmile.drop.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDropRuleTest {
    @Test
    fun `starts in idle`() {
        val item = newLenientDropRule("code-743", "label-272")
        assertEquals(LenientDropRuleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLenientDropRule("code-743", "label-272")
        try {
            item.moveTo(LenientDropRuleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDropRuleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLenientDropRule("code-743", "label-272")
        item.status = LenientDropRuleStatus.STOPPED
        assertFails("move") { item.moveTo(LenientDropRuleStatus.IDLE) }
    }
}
