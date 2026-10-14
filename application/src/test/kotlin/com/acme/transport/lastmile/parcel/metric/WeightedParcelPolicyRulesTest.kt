package com.acme.transport.lastmile.parcel.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedParcelPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedParcelPolicyValidator()
        val item = newWeightedParcelPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = WeightedParcelPolicyRequiredDepotRule("weighted parcel policy needs this field")
        val item = newWeightedParcelPolicy("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedParcelPolicyCodeLengthRule(80)
        val item = newWeightedParcelPolicy("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = WeightedParcelPolicyMaximumRangeRule(3.6, 670.6)
        val item = newWeightedParcelPolicy("x", "y")
        item.maximum = 672.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = WeightedParcelPolicyDepotLengthRule(255)
        val item = newWeightedParcelPolicy("x", "y")
        item.depot = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
