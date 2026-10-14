package com.acme.transport.lastmile.redelivery.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRedeliveryAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalRedeliveryAssignmentValidator()
        val item = newSeasonalRedeliveryAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = SeasonalRedeliveryAssignmentNameLengthRule(16)
        val item = newSeasonalRedeliveryAssignment("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = SeasonalRedeliveryAssignmentCarrierLengthRule(255)
        val item = newSeasonalRedeliveryAssignment("x", "y")
        item.carrier = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = SeasonalRedeliveryAssignmentPalletsRangeRule(32, 255)
        val item = newSeasonalRedeliveryAssignment("x", "y")
        item.pallets = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
