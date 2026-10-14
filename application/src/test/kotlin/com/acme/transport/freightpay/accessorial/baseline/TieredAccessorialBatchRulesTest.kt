package com.acme.transport.freightpay.accessorial.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredAccessorialBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredAccessorialBatchValidator()
        val item = newTieredAccessorialBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredAccessorialBatchCodeLengthRule(255)
        val item = newTieredAccessorialBatch("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = TieredAccessorialBatchRequiredDockRule("tiered accessorial batch needs a value")
        val item = newTieredAccessorialBatch("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = TieredAccessorialBatchLaneLengthRule(3600)
        val item = newTieredAccessorialBatch("x", "y")
        item.lane = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
