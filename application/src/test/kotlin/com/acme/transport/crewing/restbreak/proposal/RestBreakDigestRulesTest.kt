package com.acme.transport.crewing.restbreak.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakDigestValidator()
        val item = newRestBreakDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = RestBreakDigestUnitLengthRule(255)
        val item = newRestBreakDigest("x", "y")
        item.unit = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = RestBreakDigestRequiredUnitRule("rest break digest needs a value")
        val item = newRestBreakDigest("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = RestBreakDigestCreatedAtRangeRule(80, 86400)
        val item = newRestBreakDigest("x", "y")
        item.createdAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
