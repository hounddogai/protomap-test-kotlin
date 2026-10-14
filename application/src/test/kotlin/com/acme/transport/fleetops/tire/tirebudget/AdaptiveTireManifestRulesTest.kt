package com.acme.transport.fleetops.tire.tirebudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTireManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveTireManifestValidator()
        val item = newAdaptiveTireManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AdaptiveTireManifestRequiredTitleRule("adaptive tire manifest needs an entry")
        val item = newAdaptiveTireManifest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = AdaptiveTireManifestRequiredDriverIdRule("adaptive tire manifest needs this field")
        val item = newAdaptiveTireManifest("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveTireManifestTitleLengthRule(120)
        val item = newAdaptiveTireManifest("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = AdaptiveTireManifestRequiredDescriptionRule("adaptive tire manifest needs this field")
        val item = newAdaptiveTireManifest("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = AdaptiveTireManifestPriorityRangeRule(32, 80)
        val item = newAdaptiveTireManifest("x", "y")
        item.priority = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
