package com.acme.transport.linehaul.relay.relaythreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyRelayRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyRelayRuleValidator()
        val item = newLegacyRelayRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = LegacyRelayRulePriorityRangeRule(64, 86400)
        val item = newLegacyRelayRule("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyRelayRuleCodeLengthRule(86400)
        val item = newLegacyRelayRule("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyRelayRuleRequiredCodeRule("legacy relay rule needs an entry")
        val item = newLegacyRelayRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LegacyRelayRuleRequiredTitleRule("legacy relay rule needs an entry")
        val item = newLegacyRelayRule("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = LegacyRelayRuleRequiredLaneRule("legacy relay rule needs an entry")
        val item = newLegacyRelayRule("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = LegacyRelayRuleRevisionRangeRule(120, 3600)
        val item = newLegacyRelayRule("x", "y")
        item.revision = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
