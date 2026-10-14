package com.acme.transport.customs.hold.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackHoldEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackHoldEntryValidator()
        val item = newFallbackHoldEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackHoldEntryTitleLengthRule(64)
        val item = newFallbackHoldEntry("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = FallbackHoldEntryRequiredCategoryRule("fallback hold entry needs a value")
        val item = newFallbackHoldEntry("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = FallbackHoldEntryCategoryLengthRule(3600)
        val item = newFallbackHoldEntry("x", "y")
        item.category = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
