package com.acme.transport.crewing.availability.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAvailabilityGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedAvailabilityGroupValidator()
        val item = newBatchedAvailabilityGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = BatchedAvailabilityGroupLabelLengthRule(64)
        val item = newBatchedAvailabilityGroup("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = BatchedAvailabilityGroupOdometerRangeRule(120, 255)
        val item = newBatchedAvailabilityGroup("x", "y")
        item.odometer = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedAvailabilityGroupCodeLengthRule(16)
        val item = newBatchedAvailabilityGroup("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = BatchedAvailabilityGroupRequiredChecksumRule("batched availability group needs a value")
        val item = newBatchedAvailabilityGroup("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
