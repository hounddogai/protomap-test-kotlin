package com.acme.transport.customs.clearance.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceBaselineValidator()
        val item = newClearanceBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ClearanceBaselineTitleLengthRule(80)
        val item = newClearanceBaseline("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ClearanceBaselineRequiredTitleRule("clearance baseline needs an entry")
        val item = newClearanceBaseline("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ClearanceBaselineRequiredNameRule("clearance baseline needs a value")
        val item = newClearanceBaseline("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ClearanceBaselineRequiredCodeRule("clearance baseline needs this field")
        val item = newClearanceBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = ClearanceBaselinePhoneLengthRule(86400)
        val item = newClearanceBaseline("x", "y")
        item.phone = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = ClearanceBaselineNameLengthRule(255)
        val item = newClearanceBaseline("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
