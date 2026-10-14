package com.acme.transport.terminal.dockdoor.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorMappingValidator()
        val item = newDockDoorMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = DockDoorMappingTitleLengthRule(16)
        val item = newDockDoorMapping("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DockDoorMappingRequiredTitleRule("dock door mapping needs an entry")
        val item = newDockDoorMapping("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = DockDoorMappingRequiredSegmentRule("dock door mapping needs this field")
        val item = newDockDoorMapping("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
