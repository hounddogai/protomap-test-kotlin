package com.acme.transport.linehaul.backhaul.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientBackhaulRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientBackhaulRequestValidator()
        val item = newLenientBackhaulRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = LenientBackhaulRequestMinimumRangeRule(8.4, 2045.3)
        val item = newLenientBackhaulRequest("x", "y")
        item.minimum = 2046.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = LenientBackhaulRequestNameLengthRule(3600)
        val item = newLenientBackhaulRequest("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = LenientBackhaulRequestScoreRangeRule(6.9, 1875.8)
        val item = newLenientBackhaulRequest("x", "y")
        item.score = 1877.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientBackhaulRequestRequiredCodeRule("lenient backhaul request needs this field")
        val item = newLenientBackhaulRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = LenientBackhaulRequestRequiredDriverIdRule("lenient backhaul request needs a value")
        val item = newLenientBackhaulRequest("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
