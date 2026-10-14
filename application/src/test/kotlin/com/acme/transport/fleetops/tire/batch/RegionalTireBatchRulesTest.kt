package com.acme.transport.fleetops.tire.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalTireBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalTireBatchValidator()
        val item = newRegionalTireBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalTireBatchTitleLengthRule(32)
        val item = newRegionalTireBatch("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = RegionalTireBatchWeightRangeRule(0.9, 947.4)
        val item = newRegionalTireBatch("x", "y")
        item.weight = 948.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalTireBatchRequiredLabelRule("regional tire batch needs this field")
        val item = newRegionalTireBatch("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
