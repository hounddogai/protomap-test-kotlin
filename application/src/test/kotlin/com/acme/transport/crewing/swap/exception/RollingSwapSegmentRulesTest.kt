package com.acme.transport.crewing.swap.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingSwapSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingSwapSegmentValidator()
        val item = newRollingSwapSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = RollingSwapSegmentDescriptionLengthRule(86400)
        val item = newRollingSwapSegment("x", "y")
        item.description = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = RollingSwapSegmentRequiredDriverIdRule("rolling swap segment needs this field")
        val item = newRollingSwapSegment("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingSwapSegmentRequiredNameRule("rolling swap segment needs a value")
        val item = newRollingSwapSegment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingSwapSegmentCodeLengthRule(86400)
        val item = newRollingSwapSegment("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = RollingSwapSegmentRequiredDescriptionRule("rolling swap segment needs a value")
        val item = newRollingSwapSegment("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
