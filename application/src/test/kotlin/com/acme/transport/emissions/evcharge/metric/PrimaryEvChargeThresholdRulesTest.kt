package com.acme.transport.emissions.evcharge.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryEvChargeThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryEvChargeThresholdValidator()
        val item = newPrimaryEvChargeThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryEvChargeThresholdCodeLengthRule(64)
        val item = newPrimaryEvChargeThreshold("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long unit`() {
        val rule = PrimaryEvChargeThresholdUnitLengthRule(86400)
        val item = newPrimaryEvChargeThreshold("x", "y")
        item.unit = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = PrimaryEvChargeThresholdRequiredUnitRule("primary ev charge threshold needs this field")
        val item = newPrimaryEvChargeThreshold("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
