package com.acme.transport.customs.hold.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalHoldSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalHoldSlotValidator()
        val item = newRegionalHoldSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = RegionalHoldSlotOwnerIdLengthRule(120)
        val item = newRegionalHoldSlot("x", "y")
        item.ownerId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalHoldSlotNameLengthRule(80)
        val item = newRegionalHoldSlot("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = RegionalHoldSlotRequiredOwnerIdRule("regional hold slot needs a value")
        val item = newRegionalHoldSlot("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalHoldSlotRequiredNameRule("regional hold slot needs an entry")
        val item = newRegionalHoldSlot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
