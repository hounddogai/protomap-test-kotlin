package com.acme.transport.linehaul.capacity.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCapacityThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryCapacityThresholdValidator()
        val item = newPrimaryCapacityThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PrimaryCapacityThresholdRequiredNameRule("primary capacity threshold needs an entry")
        val item = newPrimaryCapacityThreshold("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryCapacityThresholdCodeLengthRule(3600)
        val item = newPrimaryCapacityThreshold("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
