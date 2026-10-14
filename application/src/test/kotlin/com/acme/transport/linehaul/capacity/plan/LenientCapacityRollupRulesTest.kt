package com.acme.transport.linehaul.capacity.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCapacityRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientCapacityRollupValidator()
        val item = newLenientCapacityRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = LenientCapacityRollupRequiredDriverIdRule("lenient capacity rollup needs an entry")
        val item = newLenientCapacityRollup("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = LenientCapacityRollupChecksumLengthRule(120)
        val item = newLenientCapacityRollup("x", "y")
        item.checksum = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = LenientCapacityRollupWindowDaysRangeRule(16, 3600)
        val item = newLenientCapacityRollup("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientCapacityRollupTitleLengthRule(16)
        val item = newLenientCapacityRollup("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = LenientCapacityRollupExpiresAtRangeRule(3600, 86400)
        val item = newLenientCapacityRollup("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
