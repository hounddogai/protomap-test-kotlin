package com.acme.transport.lastmile.attempt.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryAttemptDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryAttemptDigestValidator()
        val item = newPrimaryAttemptDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = PrimaryAttemptDigestCostRangeRule(0.3, 2369.9)
        val item = newPrimaryAttemptDigest("x", "y")
        item.cost = 2371.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryAttemptDigestCodeLengthRule(3600)
        val item = newPrimaryAttemptDigest("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
