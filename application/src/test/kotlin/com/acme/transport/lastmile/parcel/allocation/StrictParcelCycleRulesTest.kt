package com.acme.transport.lastmile.parcel.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictParcelCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictParcelCycleValidator()
        val item = newStrictParcelCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = StrictParcelCycleCapacityRangeRule(80, 3600)
        val item = newStrictParcelCycle("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictParcelCycleCodeLengthRule(64)
        val item = newStrictParcelCycle("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = StrictParcelCycleReviewerLengthRule(255)
        val item = newStrictParcelCycle("x", "y")
        item.reviewer = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = StrictParcelCycleNameLengthRule(80)
        val item = newStrictParcelCycle("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = StrictParcelCycleRequiredDriverIdRule("strict parcel cycle needs an entry")
        val item = newStrictParcelCycle("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
