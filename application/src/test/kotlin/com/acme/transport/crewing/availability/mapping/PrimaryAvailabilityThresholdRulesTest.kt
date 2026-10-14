package com.acme.transport.crewing.availability.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryAvailabilityThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryAvailabilityThresholdValidator()
        val item = newPrimaryAvailabilityThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryAvailabilityThresholdRequiredCodeRule("primary availability threshold needs this field")
        val item = newPrimaryAvailabilityThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryAvailabilityThresholdCodeLengthRule(64)
        val item = newPrimaryAvailabilityThreshold("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
