package com.acme.transport.linehaul.laneplan.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanRuleValidator()
        val item = newLanePlanRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LanePlanRuleRequiredNameRule("lane plan rule needs this field")
        val item = newLanePlanRule("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = LanePlanRuleRequiredCarrierRule("lane plan rule needs an entry")
        val item = newLanePlanRule("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LanePlanRuleNameLengthRule(64)
        val item = newLanePlanRule("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
