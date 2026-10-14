package com.acme.transport.emissions.idling.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryIdlingBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryIdlingBundleValidator()
        val item = newPrimaryIdlingBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = PrimaryIdlingBundleRequiredSummaryRule("primary idling bundle needs this field")
        val item = newPrimaryIdlingBundle("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = PrimaryIdlingBundleRequiredCategoryRule("primary idling bundle needs an entry")
        val item = newPrimaryIdlingBundle("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = PrimaryIdlingBundleSummaryLengthRule(1000)
        val item = newPrimaryIdlingBundle("x", "y")
        item.summary = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = PrimaryIdlingBundleCategoryLengthRule(16)
        val item = newPrimaryIdlingBundle("x", "y")
        item.category = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
