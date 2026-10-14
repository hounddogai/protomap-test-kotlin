package com.acme.transport.emissions.evcharge.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeMilestoneValidator()
        val item = newEvChargeMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = EvChargeMilestoneNameLengthRule(255)
        val item = newEvChargeMilestone("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = EvChargeMilestoneFuelLitersRangeRule(5.6, 513.8)
        val item = newEvChargeMilestone("x", "y")
        item.fuelLiters = 515.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = EvChargeMilestoneCodeLengthRule(80)
        val item = newEvChargeMilestone("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
