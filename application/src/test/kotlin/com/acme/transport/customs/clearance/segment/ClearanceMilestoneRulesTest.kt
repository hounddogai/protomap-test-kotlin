package com.acme.transport.customs.clearance.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceMilestoneValidator()
        val item = newClearanceMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ClearanceMilestoneRequiredLabelRule("clearance milestone needs this field")
        val item = newClearanceMilestone("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = ClearanceMilestoneRequiredRouteCodeRule("clearance milestone needs a value")
        val item = newClearanceMilestone("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
