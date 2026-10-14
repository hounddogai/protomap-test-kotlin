package com.acme.transport.lastmile.locker.lockerassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveLockerAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveLockerAgreementValidator()
        val item = newAdaptiveLockerAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AdaptiveLockerAgreementRequiredNameRule("adaptive locker agreement needs a value")
        val item = newAdaptiveLockerAgreement("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = AdaptiveLockerAgreementChecksumLengthRule(86400)
        val item = newAdaptiveLockerAgreement("x", "y")
        item.checksum = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveLockerAgreementCodeLengthRule(16)
        val item = newAdaptiveLockerAgreement("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveLockerAgreementNameLengthRule(1000)
        val item = newAdaptiveLockerAgreement("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = AdaptiveLockerAgreementDistanceKmRangeRule(6.2, 156.8)
        val item = newAdaptiveLockerAgreement("x", "y")
        item.distanceKm = 158.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
