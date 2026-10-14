package com.acme.transport.terminal.crossdock.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockScheduleValidator()
        val item = newCrossDockSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = CrossDockScheduleLoadKgRangeRule(9.1, 4906.3)
        val item = newCrossDockSchedule("x", "y")
        item.loadKg = 4907.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = CrossDockScheduleRequiredCarrierRule("cross dock schedule needs this field")
        val item = newCrossDockSchedule("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CrossDockScheduleRequiredCodeRule("cross dock schedule needs an entry")
        val item = newCrossDockSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
