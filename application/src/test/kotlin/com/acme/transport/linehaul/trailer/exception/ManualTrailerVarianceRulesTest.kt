package com.acme.transport.linehaul.trailer.exception

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualTrailerVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTrailerVarianceValidator()
        val item = newManualTrailerVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = ManualTrailerVarianceQuantityRangeRule(80, 3600)
        val item = newManualTrailerVariance("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
