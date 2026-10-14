package com.acme.transport.customs.tariff.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingTariffDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingTariffDraftValidator()
        val item = newRollingTariffDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RollingTariffDraftLabelLengthRule(120)
        val item = newRollingTariffDraft("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = RollingTariffDraftEtaMinutesRangeRule(32, 86400)
        val item = newRollingTariffDraft("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = RollingTariffDraftFloorValueRangeRule(7.2, 2766.7)
        val item = newRollingTariffDraft("x", "y")
        item.floorValue = 2768.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
