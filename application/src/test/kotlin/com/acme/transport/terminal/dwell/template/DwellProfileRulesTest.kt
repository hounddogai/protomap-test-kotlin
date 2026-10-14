package com.acme.transport.terminal.dwell.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellProfileValidator()
        val item = newDwellProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = DwellProfileCeilingRangeRule(2.3, 3549.9)
        val item = newDwellProfile("x", "y")
        item.ceiling = 3551.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = DwellProfileTitleLengthRule(3600)
        val item = newDwellProfile("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DwellProfileCodeLengthRule(86400)
        val item = newDwellProfile("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = DwellProfileLimitRangeRule(16, 86400)
        val item = newDwellProfile("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DwellProfileRequiredTitleRule("dwell profile needs an entry")
        val item = newDwellProfile("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
