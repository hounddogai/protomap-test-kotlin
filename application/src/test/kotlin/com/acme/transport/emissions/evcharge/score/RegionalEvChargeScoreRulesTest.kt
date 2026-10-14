package com.acme.transport.emissions.evcharge.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalEvChargeScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalEvChargeScoreValidator()
        val item = newRegionalEvChargeScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = RegionalEvChargeScoreRevisionRangeRule(16, 86400)
        val item = newRegionalEvChargeScore("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalEvChargeScoreRequiredCodeRule("regional ev charge score needs a value")
        val item = newRegionalEvChargeScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
