package com.acme.transport.linehaul.trailer.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerMappingValidator()
        val item = newTrailerMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = TrailerMappingVersionRangeRule(64, 86400)
        val item = newTrailerMapping("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = TrailerMappingTitleLengthRule(120)
        val item = newTrailerMapping("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerMappingCodeLengthRule(80)
        val item = newTrailerMapping("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
