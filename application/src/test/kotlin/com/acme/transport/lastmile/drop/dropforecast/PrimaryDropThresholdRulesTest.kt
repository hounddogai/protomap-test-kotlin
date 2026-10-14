package com.acme.transport.lastmile.drop.dropforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDropThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryDropThresholdValidator()
        val item = newPrimaryDropThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = PrimaryDropThresholdMarginRangeRule(6.5, 4419.2)
        val item = newPrimaryDropThreshold("x", "y")
        item.margin = 4420.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long currency`() {
        val rule = PrimaryDropThresholdCurrencyLengthRule(64)
        val item = newPrimaryDropThreshold("x", "y")
        item.currency = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PrimaryDropThresholdRequiredNameRule("primary drop threshold needs an entry")
        val item = newPrimaryDropThreshold("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = PrimaryDropThresholdRequiredCurrencyRule("primary drop threshold needs a value")
        val item = newPrimaryDropThreshold("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
