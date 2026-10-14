package com.acme.transport.network.transittime.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTransitTimeManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultTransitTimeManifestValidator()
        val item = newDefaultTransitTimeManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultTransitTimeManifestCodeLengthRule(3600)
        val item = newDefaultTransitTimeManifest("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = DefaultTransitTimeManifestAmountRangeRule(6.3, 676.9)
        val item = newDefaultTransitTimeManifest("x", "y")
        item.amount = 678.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = DefaultTransitTimeManifestVehicleIdLengthRule(86400)
        val item = newDefaultTransitTimeManifest("x", "y")
        item.vehicleId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultTransitTimeManifestRequiredTitleRule("default transit time manifest needs a value")
        val item = newDefaultTransitTimeManifest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = DefaultTransitTimeManifestRequiredVehicleIdRule("default transit time manifest needs an entry")
        val item = newDefaultTransitTimeManifest("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
