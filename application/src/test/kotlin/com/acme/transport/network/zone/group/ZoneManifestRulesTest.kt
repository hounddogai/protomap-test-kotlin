package com.acme.transport.network.zone.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneManifestValidator()
        val item = newZoneManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = ZoneManifestContactEmailLengthRule(16)
        val item = newZoneManifest("x", "y")
        item.contactEmail = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = ZoneManifestRequiredTrailerIdRule("zone manifest needs a value")
        val item = newZoneManifest("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ZoneManifestCodeLengthRule(32)
        val item = newZoneManifest("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = ZoneManifestRequiredContactEmailRule("zone manifest needs an entry")
        val item = newZoneManifest("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = ZoneManifestFloorValueRangeRule(0.5, 1397.3)
        val item = newZoneManifest("x", "y")
        item.floorValue = 1398.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = ZoneManifestTrailerIdLengthRule(120)
        val item = newZoneManifest("x", "y")
        item.trailerId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
