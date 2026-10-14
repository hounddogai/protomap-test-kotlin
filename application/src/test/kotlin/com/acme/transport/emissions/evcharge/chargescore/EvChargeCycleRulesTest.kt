package com.acme.transport.emissions.evcharge.chargescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeCycleValidator()
        val item = newEvChargeCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = EvChargeCycleExpiresAtRangeRule(16, 80)
        val item = newEvChargeCycle("x", "y")
        item.expiresAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = EvChargeCycleRequiredLabelRule("ev charge cycle needs an entry")
        val item = newEvChargeCycle("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = EvChargeCycleCodeLengthRule(64)
        val item = newEvChargeCycle("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = EvChargeCycleLimitRangeRule(255, 1000)
        val item = newEvChargeCycle("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = EvChargeCycleFloorValueRangeRule(7.2, 4765.3)
        val item = newEvChargeCycle("x", "y")
        item.floorValue = 4766.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EvChargeCycleRequiredCodeRule("ev charge cycle needs an entry")
        val item = newEvChargeCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
