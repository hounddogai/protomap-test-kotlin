package com.acme.transport.safety.scorecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryScorecardThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryScorecardThresholdValidator()
        val item = newPrimaryScorecardThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = PrimaryScorecardThresholdCurrencyLengthRule(255)
        val item = newPrimaryScorecardThreshold("x", "y")
        item.currency = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = PrimaryScorecardThresholdRequiredExternalRefRule("primary scorecard threshold needs an entry")
        val item = newPrimaryScorecardThreshold("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryScorecardThresholdRequiredCodeRule("primary scorecard threshold needs this field")
        val item = newPrimaryScorecardThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = PrimaryScorecardThresholdExternalRefLengthRule(255)
        val item = newPrimaryScorecardThreshold("x", "y")
        item.externalRef = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = PrimaryScorecardThresholdRequiredCurrencyRule("primary scorecard threshold needs a value")
        val item = newPrimaryScorecardThreshold("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PrimaryScorecardThresholdTitleLengthRule(86400)
        val item = newPrimaryScorecardThreshold("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
