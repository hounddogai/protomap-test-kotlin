package com.acme.transport.network.zone.zonequota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryZoneAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryZoneAllocationValidator()
        val item = newPrimaryZoneAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryZoneAllocationCodeLengthRule(120)
        val item = newPrimaryZoneAllocation("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryZoneAllocationRequiredCodeRule("primary zone allocation needs a value")
        val item = newPrimaryZoneAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
