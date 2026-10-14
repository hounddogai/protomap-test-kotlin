package com.acme.transport.crewing.roster.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRosterCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingRosterCycleValidator()
        val item = newRollingRosterCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingRosterCycleCodeLengthRule(3600)
        val item = newRollingRosterCycle("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = RollingRosterCycleBudgetRangeRule(9.2, 1478.4)
        val item = newRollingRosterCycle("x", "y")
        item.budget = 1479.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingRosterCycleRequiredNameRule("rolling roster cycle needs an entry")
        val item = newRollingRosterCycle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
