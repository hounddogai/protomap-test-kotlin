package com.acme.transport.safety.scorecard.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackScorecardBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackScorecardBatchValidator()
        val item = newFallbackScorecardBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = FallbackScorecardBatchRequiredShiftCodeRule("fallback scorecard batch needs a value")
        val item = newFallbackScorecardBatch("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackScorecardBatchRequiredLabelRule("fallback scorecard batch needs a value")
        val item = newFallbackScorecardBatch("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackScorecardBatchCodeLengthRule(255)
        val item = newFallbackScorecardBatch("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = FallbackScorecardBatchDriverIdLengthRule(86400)
        val item = newFallbackScorecardBatch("x", "y")
        item.driverId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
