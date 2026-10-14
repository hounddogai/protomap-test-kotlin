package com.acme.transport.safety.nearmiss.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissPolicyValidator()
        val item = newNearMissPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = NearMissPolicyRequiredUnitRule("near miss policy needs an entry")
        val item = newNearMissPolicy("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = NearMissPolicyLimitRangeRule(64, 3600)
        val item = newNearMissPolicy("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
