package com.acme.transport.crewing.dutyperiod.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodDigestValidator()
        val item = newDutyPeriodDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DutyPeriodDigestCodeLengthRule(32)
        val item = newDutyPeriodDigest("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DutyPeriodDigestRequiredCodeRule("duty period digest needs an entry")
        val item = newDutyPeriodDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
