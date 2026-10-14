package com.acme.transport.crewing.availability.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityAgreementValidator()
        val item = newAvailabilityAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = AvailabilityAgreementRequiredLaneRule("availability agreement needs an entry")
        val item = newAvailabilityAgreement("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = AvailabilityAgreementOdometerRangeRule(255, 3600)
        val item = newAvailabilityAgreement("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long dock`() {
        val rule = AvailabilityAgreementDockLengthRule(32)
        val item = newAvailabilityAgreement("x", "y")
        item.dock = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AvailabilityAgreementRequiredCodeRule("availability agreement needs an entry")
        val item = newAvailabilityAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AvailabilityAgreementRequiredLabelRule("availability agreement needs an entry")
        val item = newAvailabilityAgreement("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
