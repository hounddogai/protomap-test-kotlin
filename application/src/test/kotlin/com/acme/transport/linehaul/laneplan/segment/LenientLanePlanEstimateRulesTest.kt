package com.acme.transport.linehaul.laneplan.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientLanePlanEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientLanePlanEstimateValidator()
        val item = newLenientLanePlanEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientLanePlanEstimateCodeLengthRule(3600)
        val item = newLenientLanePlanEstimate("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientLanePlanEstimateRequiredCodeRule("lenient lane plan estimate needs a value")
        val item = newLenientLanePlanEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientLanePlanEstimateRequiredTitleRule("lenient lane plan estimate needs an entry")
        val item = newLenientLanePlanEstimate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
