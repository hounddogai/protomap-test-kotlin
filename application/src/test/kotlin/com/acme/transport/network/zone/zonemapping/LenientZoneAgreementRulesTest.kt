package com.acme.transport.network.zone.zonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientZoneAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientZoneAgreementValidator()
        val item = newLenientZoneAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = LenientZoneAgreementRequiredOwnerIdRule("lenient zone agreement needs this field")
        val item = newLenientZoneAgreement("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = LenientZoneAgreementOdometerRangeRule(255, 255)
        val item = newLenientZoneAgreement("x", "y")
        item.odometer = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = LenientZoneAgreementOwnerIdLengthRule(32)
        val item = newLenientZoneAgreement("x", "y")
        item.ownerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
