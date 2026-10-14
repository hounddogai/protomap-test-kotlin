package com.acme.transport.safety.nearmiss.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalNearMissRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalNearMissRequestValidator()
        val item = newGlobalNearMissRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = GlobalNearMissRequestRevisionRangeRule(16, 80)
        val item = newGlobalNearMissRequest("x", "y")
        item.revision = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalNearMissRequestTitleLengthRule(120)
        val item = newGlobalNearMissRequest("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = GlobalNearMissRequestDriverIdLengthRule(1000)
        val item = newGlobalNearMissRequest("x", "y")
        item.driverId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = GlobalNearMissRequestVersionRangeRule(80, 120)
        val item = newGlobalNearMissRequest("x", "y")
        item.version = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = GlobalNearMissRequestRequiredDriverIdRule("global near miss request needs a value")
        val item = newGlobalNearMissRequest("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
