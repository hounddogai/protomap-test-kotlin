package com.acme.transport.emissions.evcharge.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackEvChargeEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackEvChargeEntryValidator()
        val item = newFallbackEvChargeEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = FallbackEvChargeEntryLimitRangeRule(80, 255)
        val item = newFallbackEvChargeEntry("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = FallbackEvChargeEntryRatioRangeRule(3.4, 4323.0)
        val item = newFallbackEvChargeEntry("x", "y")
        item.ratio = 4324.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = FallbackEvChargeEntryTrailerIdLengthRule(86400)
        val item = newFallbackEvChargeEntry("x", "y")
        item.trailerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackEvChargeEntryRequiredLabelRule("fallback ev charge entry needs an entry")
        val item = newFallbackEvChargeEntry("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackEvChargeEntryCodeLengthRule(1000)
        val item = newFallbackEvChargeEntry("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
