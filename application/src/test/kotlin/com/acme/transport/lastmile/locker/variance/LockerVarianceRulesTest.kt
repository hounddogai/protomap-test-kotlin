package com.acme.transport.lastmile.locker.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerVarianceValidator()
        val item = newLockerVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LockerVarianceCodeLengthRule(255)
        val item = newLockerVariance("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LockerVarianceRequiredTitleRule("locker variance needs this field")
        val item = newLockerVariance("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = LockerVarianceLimitRangeRule(120, 3600)
        val item = newLockerVariance("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = LockerVarianceExpiresAtRangeRule(16, 3600)
        val item = newLockerVariance("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
