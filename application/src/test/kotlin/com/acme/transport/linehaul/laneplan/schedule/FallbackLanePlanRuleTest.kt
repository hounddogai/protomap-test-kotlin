package com.acme.transport.linehaul.laneplan.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackLanePlanRuleTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackLanePlanRule("code-415", "label-969")
        assertEquals(FallbackLanePlanRuleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newFallbackLanePlanRule("code-415", "label-969")
        try {
            item.moveTo(FallbackLanePlanRuleStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackLanePlanRuleStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackLanePlanRule("code-415", "label-969")
        item.status = FallbackLanePlanRuleStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackLanePlanRuleStatus.DRAFT) }
    }
}
