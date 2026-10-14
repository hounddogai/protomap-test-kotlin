package com.acme.transport.linehaul.laneplan.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanRuleTest {
    @Test
    fun `starts in created`() {
        val item = newLanePlanRule("code-872", "name-599")
        assertEquals(LanePlanRuleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLanePlanRule("code-872", "name-599")
        try {
            item.moveTo(LanePlanRuleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanRuleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLanePlanRule("code-872", "name-599")
        item.status = LanePlanRuleStatus.RETIRED
        assertFails("move") { item.moveTo(LanePlanRuleStatus.CREATED) }
    }
}
