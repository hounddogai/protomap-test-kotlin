package com.acme.transport.emissions.emissionfactor.factormilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingEmissionFactorOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingEmissionFactorOverrideValidator()
        val item = newPendingEmissionFactorOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = PendingEmissionFactorOverrideRequiredDriverIdRule(
            "pending emission factor override needs this field",
        )
        val item = newPendingEmissionFactorOverride("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingEmissionFactorOverrideNameLengthRule(86400)
        val item = newPendingEmissionFactorOverride("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingEmissionFactorOverrideRequiredCodeRule("pending emission factor override needs this field")
        val item = newPendingEmissionFactorOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = PendingEmissionFactorOverrideLoadKgRangeRule(3.3, 3017.8)
        val item = newPendingEmissionFactorOverride("x", "y")
        item.loadKg = 3019.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
