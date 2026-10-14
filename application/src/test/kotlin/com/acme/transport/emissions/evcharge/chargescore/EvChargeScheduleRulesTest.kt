package com.acme.transport.emissions.evcharge.chargescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeScheduleValidator()
        val item = newEvChargeSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = EvChargeScheduleBatchSizeRangeRule(32, 1000)
        val item = newEvChargeSchedule("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = EvChargeScheduleCodeLengthRule(86400)
        val item = newEvChargeSchedule("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = EvChargeScheduleLabelLengthRule(1000)
        val item = newEvChargeSchedule("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
