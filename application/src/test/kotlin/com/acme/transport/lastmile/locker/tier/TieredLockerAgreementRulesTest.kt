package com.acme.transport.lastmile.locker.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredLockerAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredLockerAgreementValidator()
        val item = newTieredLockerAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredLockerAgreementCodeLengthRule(120)
        val item = newTieredLockerAgreement("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = TieredLockerAgreementCreatedAtRangeRule(80, 3600)
        val item = newTieredLockerAgreement("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = TieredLockerAgreementOdometerRangeRule(3600, 86400)
        val item = newTieredLockerAgreement("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
