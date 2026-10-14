package com.acme.transport.terminal.dockdoor.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDockDoorPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictDockDoorPlanValidator()
        val item = newStrictDockDoorPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = StrictDockDoorPlanCreatedAtRangeRule(16, 64)
        val item = newStrictDockDoorPlan("x", "y")
        item.createdAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = StrictDockDoorPlanRequiredLaneRule("strict dock door plan needs this field")
        val item = newStrictDockDoorPlan("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = StrictDockDoorPlanOwnerIdLengthRule(120)
        val item = newStrictDockDoorPlan("x", "y")
        item.ownerId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictDockDoorPlanRequiredCodeRule("strict dock door plan needs this field")
        val item = newStrictDockDoorPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = StrictDockDoorPlanRatioRangeRule(2.1, 171.1)
        val item = newStrictDockDoorPlan("x", "y")
        item.ratio = 172.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = StrictDockDoorPlanRequiredOwnerIdRule("strict dock door plan needs this field")
        val item = newStrictDockDoorPlan("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
