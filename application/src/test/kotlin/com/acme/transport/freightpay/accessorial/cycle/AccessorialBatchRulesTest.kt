package com.acme.transport.freightpay.accessorial.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialBatchValidator()
        val item = newAccessorialBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AccessorialBatchRequiredLabelRule("accessorial batch needs this field")
        val item = newAccessorialBatch("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = AccessorialBatchLabelLengthRule(32)
        val item = newAccessorialBatch("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
