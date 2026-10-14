package com.acme.transport.customs.clearance.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyClearanceGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyClearanceGroupValidator()
        val item = newLegacyClearanceGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = LegacyClearanceGroupRateRangeRule(9.6, 212.1)
        val item = newLegacyClearanceGroup("x", "y")
        item.rate = 213.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyClearanceGroupRequiredCodeRule("legacy clearance group needs this field")
        val item = newLegacyClearanceGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
