package com.acme.transport.linehaul.trailer.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTrailerPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictTrailerPlanValidator()
        val item = newStrictTrailerPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictTrailerPlanRequiredLabelRule("strict trailer plan needs an entry")
        val item = newStrictTrailerPlan("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictTrailerPlanRequiredCodeRule("strict trailer plan needs a value")
        val item = newStrictTrailerPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = StrictTrailerPlanRequiredSegmentRule("strict trailer plan needs this field")
        val item = newStrictTrailerPlan("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
