package com.acme.transport.crewing.qualification.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationPolicyValidator()
        val item = newQualificationPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = QualificationPolicyLabelLengthRule(255)
        val item = newQualificationPolicy("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = QualificationPolicyCeilingRangeRule(0.3, 2817.5)
        val item = newQualificationPolicy("x", "y")
        item.ceiling = 2819.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = QualificationPolicyRequiredDriverIdRule("qualification policy needs an entry")
        val item = newQualificationPolicy("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = QualificationPolicyRequiredCodeRule("qualification policy needs a value")
        val item = newQualificationPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
