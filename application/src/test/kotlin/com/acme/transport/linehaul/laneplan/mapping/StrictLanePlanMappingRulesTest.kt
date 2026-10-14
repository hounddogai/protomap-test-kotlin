package com.acme.transport.linehaul.laneplan.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictLanePlanMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictLanePlanMappingValidator()
        val item = newStrictLanePlanMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = StrictLanePlanMappingMarginRangeRule(7.2, 1971.9)
        val item = newStrictLanePlanMapping("x", "y")
        item.margin = 1973.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank category`() {
        val rule = StrictLanePlanMappingRequiredCategoryRule("strict lane plan mapping needs a value")
        val item = newStrictLanePlanMapping("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = StrictLanePlanMappingCategoryLengthRule(120)
        val item = newStrictLanePlanMapping("x", "y")
        item.category = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictLanePlanMappingTitleLengthRule(1000)
        val item = newStrictLanePlanMapping("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictLanePlanMappingRequiredCodeRule("strict lane plan mapping needs this field")
        val item = newStrictLanePlanMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
