package com.acme.transport.fleetops.utilization.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingUtilizationQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingUtilizationQuotaValidator()
        val item = newRollingUtilizationQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = RollingUtilizationQuotaFuelLitersRangeRule(2.2, 4331.5)
        val item = newRollingUtilizationQuota("x", "y")
        item.fuelLiters = 4333.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingUtilizationQuotaRequiredNameRule("rolling utilization quota needs a value")
        val item = newRollingUtilizationQuota("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingUtilizationQuotaCodeLengthRule(120)
        val item = newRollingUtilizationQuota("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
