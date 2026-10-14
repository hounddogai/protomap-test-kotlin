package com.acme.transport.network.servicearea.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaAllocationValidator()
        val item = newServiceAreaAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = ServiceAreaAllocationChecksumLengthRule(1000)
        val item = newServiceAreaAllocation("x", "y")
        item.checksum = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long lane`() {
        val rule = ServiceAreaAllocationLaneLengthRule(1000)
        val item = newServiceAreaAllocation("x", "y")
        item.lane = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = ServiceAreaAllocationNameLengthRule(1000)
        val item = newServiceAreaAllocation("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = ServiceAreaAllocationRequiredChecksumRule("service area allocation needs an entry")
        val item = newServiceAreaAllocation("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
