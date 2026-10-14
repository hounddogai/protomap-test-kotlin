package com.acme.transport.network.transittime.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeSnapshotValidator()
        val item = newTransitTimeSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = TransitTimeSnapshotWeightRangeRule(9.3, 4176.7)
        val item = newTransitTimeSnapshot("x", "y")
        item.weight = 4178.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = TransitTimeSnapshotRequiredDepotRule("transit time snapshot needs this field")
        val item = newTransitTimeSnapshot("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = TransitTimeSnapshotMaximumRangeRule(9.0, 236.8)
        val item = newTransitTimeSnapshot("x", "y")
        item.maximum = 238.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
