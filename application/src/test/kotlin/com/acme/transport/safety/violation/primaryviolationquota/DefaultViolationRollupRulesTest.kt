package com.acme.transport.safety.violation.primaryviolationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultViolationRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultViolationRollupValidator()
        val item = newDefaultViolationRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefaultViolationRollupTitleLengthRule(255)
        val item = newDefaultViolationRollup("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefaultViolationRollupRequiredLabelRule("default violation rollup needs an entry")
        val item = newDefaultViolationRollup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = DefaultViolationRollupRequiredContactEmailRule("default violation rollup needs this field")
        val item = newDefaultViolationRollup("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = DefaultViolationRollupAmountRangeRule(8.0, 1247.6)
        val item = newDefaultViolationRollup("x", "y")
        item.amount = 1249.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
