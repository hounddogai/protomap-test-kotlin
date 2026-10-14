package com.acme.transport.linehaul.laneplan.planrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientLanePlanRuleTest {
    @Test
    fun `starts in open`() {
        val item = newLenientLanePlanRule("code-396", "title-352")
        assertEquals(LenientLanePlanRuleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newLenientLanePlanRule("code-396", "title-352")
        try {
            item.moveTo(LenientLanePlanRuleStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientLanePlanRuleStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientLanePlanRule("code-396", "title-352")
        item.status = LenientLanePlanRuleStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientLanePlanRuleStatus.OPEN) }
    }
}
