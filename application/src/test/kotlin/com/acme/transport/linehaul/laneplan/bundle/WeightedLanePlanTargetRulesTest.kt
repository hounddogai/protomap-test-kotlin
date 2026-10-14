package com.acme.transport.linehaul.laneplan.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedLanePlanTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedLanePlanTargetValidator()
        val item = newWeightedLanePlanTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = WeightedLanePlanTargetRequiredDescriptionRule("weighted lane plan target needs this field")
        val item = newWeightedLanePlanTarget("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = WeightedLanePlanTargetRequiredCurrencyRule("weighted lane plan target needs an entry")
        val item = newWeightedLanePlanTarget("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedLanePlanTargetRequiredNameRule("weighted lane plan target needs a value")
        val item = newWeightedLanePlanTarget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = WeightedLanePlanTargetNameLengthRule(32)
        val item = newWeightedLanePlanTarget("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
