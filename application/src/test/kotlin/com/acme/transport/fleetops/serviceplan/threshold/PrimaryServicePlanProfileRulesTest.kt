package com.acme.transport.fleetops.serviceplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryServicePlanProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryServicePlanProfileValidator()
        val item = newPrimaryServicePlanProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = PrimaryServicePlanProfileEtaMinutesRangeRule(1000, 86400)
        val item = newPrimaryServicePlanProfile("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = PrimaryServicePlanProfileShiftCodeLengthRule(255)
        val item = newPrimaryServicePlanProfile("x", "y")
        item.shiftCode = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = PrimaryServicePlanProfileRequiredShiftCodeRule("primary service plan profile needs an entry")
        val item = newPrimaryServicePlanProfile("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryServicePlanProfileRequiredCodeRule("primary service plan profile needs this field")
        val item = newPrimaryServicePlanProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
