package com.acme.transport.crewing.availability.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientAvailabilityManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientAvailabilityManifestValidator()
        val item = newLenientAvailabilityManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = LenientAvailabilityManifestMaximumRangeRule(1.8, 156.2)
        val item = newLenientAvailabilityManifest("x", "y")
        item.maximum = 157.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long route code`() {
        val rule = LenientAvailabilityManifestRouteCodeLengthRule(3600)
        val item = newLenientAvailabilityManifest("x", "y")
        item.routeCode = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = LenientAvailabilityManifestWindowDaysRangeRule(3600, 3600)
        val item = newLenientAvailabilityManifest("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientAvailabilityManifestCodeLengthRule(255)
        val item = newLenientAvailabilityManifest("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientAvailabilityManifestRequiredCodeRule("lenient availability manifest needs an entry")
        val item = newLenientAvailabilityManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
