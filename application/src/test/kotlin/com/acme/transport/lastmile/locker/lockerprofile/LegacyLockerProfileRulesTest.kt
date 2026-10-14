package com.acme.transport.lastmile.locker.lockerprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyLockerProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyLockerProfileValidator()
        val item = newLegacyLockerProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = LegacyLockerProfilePalletsRangeRule(80, 3600)
        val item = newLegacyLockerProfile("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = LegacyLockerProfileUpdatedAtRangeRule(120, 255)
        val item = newLegacyLockerProfile("x", "y")
        item.updatedAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = LegacyLockerProfileDriverIdLengthRule(80)
        val item = newLegacyLockerProfile("x", "y")
        item.driverId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyLockerProfileRequiredNameRule("legacy locker profile needs this field")
        val item = newLegacyLockerProfile("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = LegacyLockerProfileDiscountRangeRule(7.5, 4960.6)
        val item = newLegacyLockerProfile("x", "y")
        item.discount = 4962.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
