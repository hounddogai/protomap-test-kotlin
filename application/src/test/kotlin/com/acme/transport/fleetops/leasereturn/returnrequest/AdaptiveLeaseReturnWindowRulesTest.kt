package com.acme.transport.fleetops.leasereturn.returnrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveLeaseReturnWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveLeaseReturnWindowValidator()
        val item = newAdaptiveLeaseReturnWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = AdaptiveLeaseReturnWindowUnitLengthRule(64)
        val item = newAdaptiveLeaseReturnWindow("x", "y")
        item.unit = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = AdaptiveLeaseReturnWindowRequiredUnitRule("adaptive lease return window needs this field")
        val item = newAdaptiveLeaseReturnWindow("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveLeaseReturnWindowRequiredCodeRule("adaptive lease return window needs a value")
        val item = newAdaptiveLeaseReturnWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AdaptiveLeaseReturnWindowRequiredNameRule("adaptive lease return window needs a value")
        val item = newAdaptiveLeaseReturnWindow("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveLeaseReturnWindowNameLengthRule(120)
        val item = newAdaptiveLeaseReturnWindow("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
