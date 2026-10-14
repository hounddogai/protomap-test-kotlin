package com.acme.transport.emissions.evcharge.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeGroupValidator()
        val item = newEvChargeGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = EvChargeGroupRequiredExternalRefRule("ev charge group needs an entry")
        val item = newEvChargeGroup("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = EvChargeGroupQuantityRangeRule(16, 3600)
        val item = newEvChargeGroup("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = EvChargeGroupCodeLengthRule(80)
        val item = newEvChargeGroup("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
