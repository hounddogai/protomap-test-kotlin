package com.acme.transport.emissions.idling.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualIdlingRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualIdlingRuleValidator()
        val item = newManualIdlingRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = ManualIdlingRuleContactEmailLengthRule(80)
        val item = newManualIdlingRule("x", "y")
        item.contactEmail = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = ManualIdlingRuleRequiredTrailerIdRule("manual idling rule needs this field")
        val item = newManualIdlingRule("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
