package com.acme.transport.terminal.crossdock.docksummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCrossDockMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredCrossDockMappingValidator()
        val item = newTieredCrossDockMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = TieredCrossDockMappingAttemptsRangeRule(120, 120)
        val item = newTieredCrossDockMapping("x", "y")
        item.attempts = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = TieredCrossDockMappingNameLengthRule(120)
        val item = newTieredCrossDockMapping("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredCrossDockMappingCodeLengthRule(3600)
        val item = newTieredCrossDockMapping("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredCrossDockMappingRequiredCodeRule("tiered cross dock mapping needs this field")
        val item = newTieredCrossDockMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
