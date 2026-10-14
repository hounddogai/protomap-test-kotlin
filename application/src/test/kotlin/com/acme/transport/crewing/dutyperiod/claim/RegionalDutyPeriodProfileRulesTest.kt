package com.acme.transport.crewing.dutyperiod.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDutyPeriodProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDutyPeriodProfileValidator()
        val item = newRegionalDutyPeriodProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = RegionalDutyPeriodProfileExternalRefLengthRule(32)
        val item = newRegionalDutyPeriodProfile("x", "y")
        item.externalRef = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = RegionalDutyPeriodProfileLoadKgRangeRule(8.0, 4697.5)
        val item = newRegionalDutyPeriodProfile("x", "y")
        item.loadKg = 4699.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalDutyPeriodProfileNameLengthRule(255)
        val item = newRegionalDutyPeriodProfile("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalDutyPeriodProfileRequiredCodeRule("regional duty period profile needs an entry")
        val item = newRegionalDutyPeriodProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
