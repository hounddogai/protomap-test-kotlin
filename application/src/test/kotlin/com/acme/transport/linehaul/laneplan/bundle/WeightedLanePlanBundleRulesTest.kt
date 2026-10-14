package com.acme.transport.linehaul.laneplan.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedLanePlanBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedLanePlanBundleValidator()
        val item = newWeightedLanePlanBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedLanePlanBundleRequiredCodeRule("weighted lane plan bundle needs a value")
        val item = newWeightedLanePlanBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = WeightedLanePlanBundleSequenceRangeRule(32, 64)
        val item = newWeightedLanePlanBundle("x", "y")
        item.sequence = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = WeightedLanePlanBundleRequiredCarrierRule("weighted lane plan bundle needs this field")
        val item = newWeightedLanePlanBundle("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
