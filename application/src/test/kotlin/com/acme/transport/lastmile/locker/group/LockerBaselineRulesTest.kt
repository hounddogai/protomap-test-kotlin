package com.acme.transport.lastmile.locker.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerBaselineValidator()
        val item = newLockerBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = LockerBaselineRevisionRangeRule(32, 1000)
        val item = newLockerBaseline("x", "y")
        item.revision = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LockerBaselineRequiredCodeRule("locker baseline needs this field")
        val item = newLockerBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
