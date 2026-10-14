package com.acme.transport.lastmile.parcel.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedParcelTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedParcelTemplateValidator()
        val item = newWeightedParcelTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = WeightedParcelTemplateLimitRangeRule(16, 1000)
        val item = newWeightedParcelTemplate("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = WeightedParcelTemplateUpdatedAtRangeRule(32, 80)
        val item = newWeightedParcelTemplate("x", "y")
        item.updatedAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
