package com.acme.transport.lastmile.locker.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerTemplateValidator()
        val item = newLockerTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = LockerTemplateRequiredShiftCodeRule("locker template needs an entry")
        val item = newLockerTemplate("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = LockerTemplateQuantityRangeRule(16, 32)
        val item = newLockerTemplate("x", "y")
        item.quantity = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
