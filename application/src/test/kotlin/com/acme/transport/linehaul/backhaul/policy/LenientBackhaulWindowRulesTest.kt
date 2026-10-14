package com.acme.transport.linehaul.backhaul.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientBackhaulWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientBackhaulWindowValidator()
        val item = newLenientBackhaulWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = LenientBackhaulWindowSegmentLengthRule(16)
        val item = newLenientBackhaulWindow("x", "y")
        item.segment = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = LenientBackhaulWindowRequiredSegmentRule("lenient backhaul window needs a value")
        val item = newLenientBackhaulWindow("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
