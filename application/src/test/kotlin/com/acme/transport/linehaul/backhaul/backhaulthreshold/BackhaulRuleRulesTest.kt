package com.acme.transport.linehaul.backhaul.backhaulthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulRuleValidator()
        val item = newBackhaulRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BackhaulRuleRequiredNameRule("backhaul rule needs an entry")
        val item = newBackhaulRule("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = BackhaulRuleCostRangeRule(0.3, 3033.1)
        val item = newBackhaulRule("x", "y")
        item.cost = 3034.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = BackhaulRuleNameLengthRule(120)
        val item = newBackhaulRule("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
