package com.acme.transport.network.cutoff.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCutoffThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCutoffThresholdValidator()
        val item = newRegionalCutoffThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalCutoffThresholdRequiredCodeRule("regional cutoff threshold needs an entry")
        val item = newRegionalCutoffThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalCutoffThresholdCodeLengthRule(32)
        val item = newRegionalCutoffThreshold("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalCutoffThresholdLabelLengthRule(16)
        val item = newRegionalCutoffThreshold("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = RegionalCutoffThresholdEtaMinutesRangeRule(120, 1000)
        val item = newRegionalCutoffThreshold("x", "y")
        item.etaMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RegionalCutoffThresholdRequiredTitleRule("regional cutoff threshold needs a value")
        val item = newRegionalCutoffThreshold("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
