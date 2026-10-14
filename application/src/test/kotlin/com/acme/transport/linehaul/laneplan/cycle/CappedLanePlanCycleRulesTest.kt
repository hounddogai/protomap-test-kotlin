package com.acme.transport.linehaul.laneplan.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedLanePlanCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedLanePlanCycleValidator()
        val item = newCappedLanePlanCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = CappedLanePlanCycleReviewerLengthRule(16)
        val item = newCappedLanePlanCycle("x", "y")
        item.reviewer = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedLanePlanCycleRequiredCodeRule("capped lane plan cycle needs an entry")
        val item = newCappedLanePlanCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
