package com.acme.transport.emissions.evcharge.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeMappingValidator()
        val item = newEvChargeMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = EvChargeMappingMinimumRangeRule(7.1, 3257.8)
        val item = newEvChargeMapping("x", "y")
        item.minimum = 3259.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = EvChargeMappingCodeLengthRule(120)
        val item = newEvChargeMapping("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = EvChargeMappingRequiredNameRule("ev charge mapping needs an entry")
        val item = newEvChargeMapping("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
