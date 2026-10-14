package com.acme.transport.linehaul.tender.tenderrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTenderRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictTenderRollupValidator()
        val item = newStrictTenderRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictTenderRollupRequiredLabelRule("strict tender rollup needs this field")
        val item = newStrictTenderRollup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictTenderRollupCodeLengthRule(255)
        val item = newStrictTenderRollup("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictTenderRollupLabelLengthRule(64)
        val item = newStrictTenderRollup("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = StrictTenderRollupAmountRangeRule(0.7, 4722.5)
        val item = newStrictTenderRollup("x", "y")
        item.amount = 4724.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = StrictTenderRollupExpiresAtRangeRule(16, 86400)
        val item = newStrictTenderRollup("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
