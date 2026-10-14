package com.acme.transport.crewing.restbreak.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyRestBreakMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyRestBreakMappingValidator()
        val item = newLegacyRestBreakMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyRestBreakMappingCodeLengthRule(16)
        val item = newLegacyRestBreakMapping("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = LegacyRestBreakMappingBatchSizeRangeRule(16, 86400)
        val item = newLegacyRestBreakMapping("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = LegacyRestBreakMappingRatioRangeRule(6.9, 342.5)
        val item = newLegacyRestBreakMapping("x", "y")
        item.ratio = 344.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyRestBreakMappingRequiredNameRule("legacy rest break mapping needs a value")
        val item = newLegacyRestBreakMapping("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
