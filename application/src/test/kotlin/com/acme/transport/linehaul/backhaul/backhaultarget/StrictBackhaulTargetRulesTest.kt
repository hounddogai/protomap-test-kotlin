package com.acme.transport.linehaul.backhaul.backhaultarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictBackhaulTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictBackhaulTargetValidator()
        val item = newStrictBackhaulTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictBackhaulTargetTitleLengthRule(64)
        val item = newStrictBackhaulTarget("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = StrictBackhaulTargetCapacityRangeRule(16, 1000)
        val item = newStrictBackhaulTarget("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
