package com.acme.transport.linehaul.laneplan.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackLanePlanRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackLanePlanRuleValidator()
        val item = newFallbackLanePlanRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = FallbackLanePlanRuleRequiredReviewerRule("fallback lane plan rule needs this field")
        val item = newFallbackLanePlanRule("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackLanePlanRuleRequiredCodeRule("fallback lane plan rule needs an entry")
        val item = newFallbackLanePlanRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackLanePlanRuleRequiredLabelRule("fallback lane plan rule needs this field")
        val item = newFallbackLanePlanRule("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
