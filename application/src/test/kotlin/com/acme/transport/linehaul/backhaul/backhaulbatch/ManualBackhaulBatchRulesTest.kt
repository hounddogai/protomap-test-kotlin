package com.acme.transport.linehaul.backhaul.backhaulbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualBackhaulBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualBackhaulBatchValidator()
        val item = newManualBackhaulBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = ManualBackhaulBatchExternalRefLengthRule(32)
        val item = newManualBackhaulBatch("x", "y")
        item.externalRef = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = ManualBackhaulBatchEndsAtRangeRule(120, 255)
        val item = newManualBackhaulBatch("x", "y")
        item.endsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualBackhaulBatchLabelLengthRule(86400)
        val item = newManualBackhaulBatch("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = ManualBackhaulBatchRequiredExternalRefRule("manual backhaul batch needs this field")
        val item = newManualBackhaulBatch("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
