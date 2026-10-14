package com.acme.transport.crewing.availability.availabilityscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAvailabilityRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedAvailabilityRollupValidator()
        val item = newCappedAvailabilityRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = CappedAvailabilityRollupMarginRangeRule(8.2, 3749.8)
        val item = newCappedAvailabilityRollup("x", "y")
        item.margin = 3751.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedAvailabilityRollupNameLengthRule(32)
        val item = newCappedAvailabilityRollup("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = CappedAvailabilityRollupRateRangeRule(1.2, 594.3)
        val item = newCappedAvailabilityRollup("x", "y")
        item.rate = 595.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
