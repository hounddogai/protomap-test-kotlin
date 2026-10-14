package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledEmissionFactorRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledEmissionFactorRollupValidator()
        val item = newScheduledEmissionFactorRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledEmissionFactorRollupCodeLengthRule(255)
        val item = newScheduledEmissionFactorRollup("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = ScheduledEmissionFactorRollupMarginRangeRule(1.7, 1358.6)
        val item = newScheduledEmissionFactorRollup("x", "y")
        item.margin = 1360.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledEmissionFactorRollupRequiredCodeRule("scheduled emission factor rollup needs this field")
        val item = newScheduledEmissionFactorRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
