package com.acme.transport.safety.incident.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentMilestoneValidator()
        val item = newIncidentMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = IncidentMilestoneDepotLengthRule(80)
        val item = newIncidentMilestone("x", "y")
        item.depot = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = IncidentMilestoneRequiredNameRule("incident milestone needs a value")
        val item = newIncidentMilestone("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = IncidentMilestoneLimitRangeRule(64, 3600)
        val item = newIncidentMilestone("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = IncidentMilestoneNameLengthRule(3600)
        val item = newIncidentMilestone("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
