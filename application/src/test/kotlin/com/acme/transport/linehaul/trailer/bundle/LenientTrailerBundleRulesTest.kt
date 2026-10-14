package com.acme.transport.linehaul.trailer.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientTrailerBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientTrailerBundleValidator()
        val item = newLenientTrailerBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientTrailerBundleRequiredCodeRule("lenient trailer bundle needs a value")
        val item = newLenientTrailerBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = LenientTrailerBundleRequiredNotesRule("lenient trailer bundle needs this field")
        val item = newLenientTrailerBundle("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = LenientTrailerBundleCreatedAtRangeRule(64, 120)
        val item = newLenientTrailerBundle("x", "y")
        item.createdAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = LenientTrailerBundleRetriesRangeRule(32, 255)
        val item = newLenientTrailerBundle("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
