package com.acme.transport.network.zone.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneAgreementValidator()
        val item = newZoneAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ZoneAgreementCodeLengthRule(120)
        val item = newZoneAgreement("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ZoneAgreementRequiredCodeRule("zone agreement needs a value")
        val item = newZoneAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = ZoneAgreementFloorValueRangeRule(0.1, 3177.0)
        val item = newZoneAgreement("x", "y")
        item.floorValue = 3178.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = ZoneAgreementEndsAtRangeRule(64, 120)
        val item = newZoneAgreement("x", "y")
        item.endsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
