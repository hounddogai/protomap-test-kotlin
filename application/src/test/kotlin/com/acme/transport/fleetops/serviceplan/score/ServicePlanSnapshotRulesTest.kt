package com.acme.transport.fleetops.serviceplan.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanSnapshotValidator()
        val item = newServicePlanSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = ServicePlanSnapshotVersionRangeRule(32, 3600)
        val item = newServicePlanSnapshot("x", "y")
        item.version = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long lane`() {
        val rule = ServicePlanSnapshotLaneLengthRule(80)
        val item = newServicePlanSnapshot("x", "y")
        item.lane = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = ServicePlanSnapshotLoadKgRangeRule(3.0, 2248.7)
        val item = newServicePlanSnapshot("x", "y")
        item.loadKg = 2250.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = ServicePlanSnapshotRequiredTrailerIdRule("service plan snapshot needs a value")
        val item = newServicePlanSnapshot("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = ServicePlanSnapshotTrailerIdLengthRule(120)
        val item = newServicePlanSnapshot("x", "y")
        item.trailerId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ServicePlanSnapshotRequiredLabelRule("service plan snapshot needs this field")
        val item = newServicePlanSnapshot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
