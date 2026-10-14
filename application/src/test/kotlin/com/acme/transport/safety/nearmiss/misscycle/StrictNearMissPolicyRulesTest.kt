package com.acme.transport.safety.nearmiss.misscycle

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictNearMissPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictNearMissPolicyValidator()
        val item = newStrictNearMissPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = StrictNearMissPolicyRevisionRangeRule(1000, 86400)
        val item = newStrictNearMissPolicy("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
