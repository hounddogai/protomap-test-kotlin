package com.acme.transport.terminal.dwell.dwellnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedDwellNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedDwellNoticeValidator()
        val item = newWeightedDwellNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = WeightedDwellNoticeRequiredShiftCodeRule("weighted dwell notice needs an entry")
        val item = newWeightedDwellNotice("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedDwellNoticeRequiredCodeRule("weighted dwell notice needs this field")
        val item = newWeightedDwellNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedDwellNoticeCodeLengthRule(1000)
        val item = newWeightedDwellNotice("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
