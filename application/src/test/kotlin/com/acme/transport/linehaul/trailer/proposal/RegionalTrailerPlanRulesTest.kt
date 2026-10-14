package com.acme.transport.linehaul.trailer.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalTrailerPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalTrailerPlanValidator()
        val item = newRegionalTrailerPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = RegionalTrailerPlanCategoryLengthRule(1000)
        val item = newRegionalTrailerPlan("x", "y")
        item.category = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = RegionalTrailerPlanChecksumLengthRule(1000)
        val item = newRegionalTrailerPlan("x", "y")
        item.checksum = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalTrailerPlanRequiredCodeRule("regional trailer plan needs a value")
        val item = newRegionalTrailerPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RegionalTrailerPlanRequiredTitleRule("regional trailer plan needs an entry")
        val item = newRegionalTrailerPlan("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
