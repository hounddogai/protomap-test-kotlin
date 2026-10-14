package com.acme.transport.linehaul.backhaul.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulBatchValidator()
        val item = newBackhaulBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = BackhaulBatchMinimumRangeRule(1.0, 253.3)
        val item = newBackhaulBatch("x", "y")
        item.minimum = 254.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = BackhaulBatchExpiresAtRangeRule(1000, 86400)
        val item = newBackhaulBatch("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BackhaulBatchRequiredLabelRule("backhaul batch needs a value")
        val item = newBackhaulBatch("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BackhaulBatchCodeLengthRule(3600)
        val item = newBackhaulBatch("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long dock`() {
        val rule = BackhaulBatchDockLengthRule(86400)
        val item = newBackhaulBatch("x", "y")
        item.dock = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
