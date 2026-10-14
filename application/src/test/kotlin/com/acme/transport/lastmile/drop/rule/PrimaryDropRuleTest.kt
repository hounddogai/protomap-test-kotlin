package com.acme.transport.lastmile.drop.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryDropRuleTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryDropRule("code-477", "label-518")
        assertEquals(PrimaryDropRuleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newPrimaryDropRule("code-477", "label-518")
        try {
            item.moveTo(PrimaryDropRuleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryDropRuleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryDropRule("code-477", "label-518")
        item.status = PrimaryDropRuleStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryDropRuleStatus.IDLE) }
    }
}
