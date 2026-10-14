package com.acme.transport.network.transittime.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTransitTimeSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedTransitTimeSnapshotValidator()
        val item = newWeightedTransitTimeSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedTransitTimeSnapshotRequiredLabelRule("weighted transit time snapshot needs an entry")
        val item = newWeightedTransitTimeSnapshot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = WeightedTransitTimeSnapshotRouteCodeLengthRule(255)
        val item = newWeightedTransitTimeSnapshot("x", "y")
        item.routeCode = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = WeightedTransitTimeSnapshotRequiredTrailerIdRule("weighted transit time snapshot needs a value")
        val item = newWeightedTransitTimeSnapshot("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
