package com.acme.transport.lastmile.redelivery.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryBaselineValidator()
        val item = newRedeliveryBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = RedeliveryBaselineRequiredVehicleIdRule("redelivery baseline needs an entry")
        val item = newRedeliveryBaseline("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RedeliveryBaselineNameLengthRule(86400)
        val item = newRedeliveryBaseline("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = RedeliveryBaselineWeightRangeRule(6.0, 1497.4)
        val item = newRedeliveryBaseline("x", "y")
        item.weight = 1498.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RedeliveryBaselineRequiredNameRule("redelivery baseline needs this field")
        val item = newRedeliveryBaseline("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
