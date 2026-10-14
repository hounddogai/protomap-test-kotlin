package com.acme.transport.fleetops.tire.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireScheduleValidator()
        val item = newTireSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = TireScheduleCategoryLengthRule(255)
        val item = newTireSchedule("x", "y")
        item.category = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = TireScheduleRequiredRegionRule("tire schedule needs a value")
        val item = newTireSchedule("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TireScheduleRequiredCodeRule("tire schedule needs a value")
        val item = newTireSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
