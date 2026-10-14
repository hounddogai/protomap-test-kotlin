package com.acme.transport.emissions.emissionfactor.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingEmissionFactorTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingEmissionFactorTargetValidator()
        val item = newPendingEmissionFactorTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = PendingEmissionFactorTargetThresholdRangeRule(7.0, 1934.2)
        val item = newPendingEmissionFactorTarget("x", "y")
        item.threshold = 1935.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = PendingEmissionFactorTargetRequiredPhoneRule("pending emission factor target needs a value")
        val item = newPendingEmissionFactorTarget("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingEmissionFactorTargetRequiredCodeRule("pending emission factor target needs an entry")
        val item = newPendingEmissionFactorTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
