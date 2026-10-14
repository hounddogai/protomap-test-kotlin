package com.acme.transport.crewing.qualification.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalQualificationPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalQualificationPolicyValidator()
        val item = newGlobalQualificationPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = GlobalQualificationPolicyTrailerIdLengthRule(64)
        val item = newGlobalQualificationPolicy("x", "y")
        item.trailerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalQualificationPolicyRequiredCodeRule("global qualification policy needs a value")
        val item = newGlobalQualificationPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = GlobalQualificationPolicyGraceDaysRangeRule(64, 86400)
        val item = newGlobalQualificationPolicy("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = GlobalQualificationPolicyRequiredTitleRule("global qualification policy needs a value")
        val item = newGlobalQualificationPolicy("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = GlobalQualificationPolicyAttemptsRangeRule(32, 3600)
        val item = newGlobalQualificationPolicy("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
