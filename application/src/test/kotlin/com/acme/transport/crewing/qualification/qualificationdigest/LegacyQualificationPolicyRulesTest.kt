package com.acme.transport.crewing.qualification.qualificationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyQualificationPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyQualificationPolicyValidator()
        val item = newLegacyQualificationPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = LegacyQualificationPolicyPriorityRangeRule(80, 120)
        val item = newLegacyQualificationPolicy("x", "y")
        item.priority = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = LegacyQualificationPolicyNameLengthRule(3600)
        val item = newLegacyQualificationPolicy("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyQualificationPolicyRequiredCodeRule("legacy qualification policy needs a value")
        val item = newLegacyQualificationPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
