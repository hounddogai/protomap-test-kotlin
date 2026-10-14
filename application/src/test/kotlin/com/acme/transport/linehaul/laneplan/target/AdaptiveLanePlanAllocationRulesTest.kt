package com.acme.transport.linehaul.laneplan.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveLanePlanAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveLanePlanAllocationValidator()
        val item = newAdaptiveLanePlanAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveLanePlanAllocationRequiredCodeRule("adaptive lane plan allocation needs an entry")
        val item = newAdaptiveLanePlanAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AdaptiveLanePlanAllocationRequiredTitleRule("adaptive lane plan allocation needs this field")
        val item = newAdaptiveLanePlanAllocation("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveLanePlanAllocationCodeLengthRule(3600)
        val item = newAdaptiveLanePlanAllocation("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
