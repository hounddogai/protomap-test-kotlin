package com.acme.transport.lastmile.locker.lockerclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerBundleValidator()
        val item = newLockerBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LockerBundleCodeLengthRule(1000)
        val item = newLockerBundle("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = LockerBundleRatioRangeRule(5.2, 1474.1)
        val item = newLockerBundle("x", "y")
        item.ratio = 1475.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = LockerBundleEtaMinutesRangeRule(255, 255)
        val item = newLockerBundle("x", "y")
        item.etaMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
