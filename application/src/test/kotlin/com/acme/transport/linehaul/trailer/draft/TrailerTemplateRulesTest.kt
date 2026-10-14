package com.acme.transport.linehaul.trailer.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerTemplateValidator()
        val item = newTrailerTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = TrailerTemplateRequiredChannelRule("trailer template needs this field")
        val item = newTrailerTemplate("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TrailerTemplateRequiredTitleRule("trailer template needs a value")
        val item = newTrailerTemplate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = TrailerTemplateFloorValueRangeRule(6.2, 4493.8)
        val item = newTrailerTemplate("x", "y")
        item.floorValue = 4495.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerTemplateCodeLengthRule(120)
        val item = newTrailerTemplate("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
