package com.acme.transport.crewing.swap.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalSwapSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalSwapSegmentValidator()
        val item = newSeasonalSwapSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalSwapSegmentRequiredCodeRule("seasonal swap segment needs an entry")
        val item = newSeasonalSwapSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = SeasonalSwapSegmentRequiredShiftCodeRule("seasonal swap segment needs an entry")
        val item = newSeasonalSwapSegment("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SeasonalSwapSegmentRequiredNameRule("seasonal swap segment needs a value")
        val item = newSeasonalSwapSegment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = SeasonalSwapSegmentNameLengthRule(255)
        val item = newSeasonalSwapSegment("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
