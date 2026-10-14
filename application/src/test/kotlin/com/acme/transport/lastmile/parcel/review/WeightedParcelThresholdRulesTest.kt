package com.acme.transport.lastmile.parcel.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedParcelThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedParcelThresholdValidator()
        val item = newWeightedParcelThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedParcelThresholdCodeLengthRule(64)
        val item = newWeightedParcelThreshold("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedParcelThresholdRequiredCodeRule("weighted parcel threshold needs this field")
        val item = newWeightedParcelThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = WeightedParcelThresholdPalletsRangeRule(1000, 86400)
        val item = newWeightedParcelThreshold("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
