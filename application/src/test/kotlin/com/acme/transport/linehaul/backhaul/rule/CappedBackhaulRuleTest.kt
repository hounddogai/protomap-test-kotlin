package com.acme.transport.linehaul.backhaul.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedBackhaulRuleTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedBackhaulRule("code-556", "label-208")
        assertEquals(CappedBackhaulRuleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCappedBackhaulRule("code-556", "label-208")
        try {
            item.moveTo(CappedBackhaulRuleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedBackhaulRuleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedBackhaulRule("code-556", "label-208")
        item.status = CappedBackhaulRuleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedBackhaulRuleStatus.PROPOSED) }
    }
}
