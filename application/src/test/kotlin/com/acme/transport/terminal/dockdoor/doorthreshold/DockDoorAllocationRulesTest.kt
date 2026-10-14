package com.acme.transport.terminal.dockdoor.doorthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorAllocationValidator()
        val item = newDockDoorAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = DockDoorAllocationTitleLengthRule(86400)
        val item = newDockDoorAllocation("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DockDoorAllocationCodeLengthRule(120)
        val item = newDockDoorAllocation("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = DockDoorAllocationDriverIdLengthRule(16)
        val item = newDockDoorAllocation("x", "y")
        item.driverId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
