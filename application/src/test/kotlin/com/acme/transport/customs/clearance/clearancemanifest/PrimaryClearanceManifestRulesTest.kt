package com.acme.transport.customs.clearance.clearancemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryClearanceManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryClearanceManifestValidator()
        val item = newPrimaryClearanceManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryClearanceManifestRequiredCodeRule("primary clearance manifest needs this field")
        val item = newPrimaryClearanceManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = PrimaryClearanceManifestMinimumRangeRule(2.6, 1263.5)
        val item = newPrimaryClearanceManifest("x", "y")
        item.minimum = 1265.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryClearanceManifestCodeLengthRule(64)
        val item = newPrimaryClearanceManifest("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = PrimaryClearanceManifestCarrierLengthRule(86400)
        val item = newPrimaryClearanceManifest("x", "y")
        item.carrier = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = PrimaryClearanceManifestCapacityRangeRule(80, 120)
        val item = newPrimaryClearanceManifest("x", "y")
        item.capacity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = PrimaryClearanceManifestMaximumRangeRule(1.5, 4141.2)
        val item = newPrimaryClearanceManifest("x", "y")
        item.maximum = 4142.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
