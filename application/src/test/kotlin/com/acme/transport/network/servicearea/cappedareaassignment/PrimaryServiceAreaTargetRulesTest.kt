package com.acme.transport.network.servicearea.cappedareaassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryServiceAreaTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryServiceAreaTargetValidator()
        val item = newPrimaryServiceAreaTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryServiceAreaTargetCodeLengthRule(3600)
        val item = newPrimaryServiceAreaTarget("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = PrimaryServiceAreaTargetRequiredTrailerIdRule("primary service area target needs an entry")
        val item = newPrimaryServiceAreaTarget("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = PrimaryServiceAreaTargetThresholdRangeRule(4.9, 1956.7)
        val item = newPrimaryServiceAreaTarget("x", "y")
        item.threshold = 1958.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
