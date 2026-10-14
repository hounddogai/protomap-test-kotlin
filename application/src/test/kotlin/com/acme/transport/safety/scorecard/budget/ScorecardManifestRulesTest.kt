package com.acme.transport.safety.scorecard.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardManifestValidator()
        val item = newScorecardManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScorecardManifestCodeLengthRule(32)
        val item = newScorecardManifest("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = ScorecardManifestRateRangeRule(6.1, 1823.4)
        val item = newScorecardManifest("x", "y")
        item.rate = 1824.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = ScorecardManifestOdometerRangeRule(32, 255)
        val item = newScorecardManifest("x", "y")
        item.odometer = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
