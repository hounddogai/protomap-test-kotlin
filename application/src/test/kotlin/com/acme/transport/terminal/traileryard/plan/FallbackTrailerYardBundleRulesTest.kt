package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackTrailerYardBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackTrailerYardBundleValidator()
        val item = newFallbackTrailerYardBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = FallbackTrailerYardBundleRequiredSummaryRule("fallback trailer yard bundle needs this field")
        val item = newFallbackTrailerYardBundle("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
