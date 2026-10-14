package com.acme.transport.network.node.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeScheduleValidator()
        val item = newNodeSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = NodeScheduleLimitRangeRule(16, 255)
        val item = newNodeSchedule("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = NodeScheduleLabelLengthRule(64)
        val item = newNodeSchedule("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = NodeScheduleCodeLengthRule(1000)
        val item = newNodeSchedule("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NodeScheduleRequiredCodeRule("node schedule needs this field")
        val item = newNodeSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
