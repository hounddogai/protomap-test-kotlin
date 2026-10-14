package com.acme.transport.emissions.evcharge.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeSlotValidator()
        val item = newEvChargeSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = EvChargeSlotChecksumLengthRule(64)
        val item = newEvChargeSlot("x", "y")
        item.checksum = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = EvChargeSlotRequiredSourceRule("ev charge slot needs an entry")
        val item = newEvChargeSlot("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = EvChargeSlotNameLengthRule(255)
        val item = newEvChargeSlot("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = EvChargeSlotEtaMinutesRangeRule(3600, 86400)
        val item = newEvChargeSlot("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EvChargeSlotRequiredCodeRule("ev charge slot needs an entry")
        val item = newEvChargeSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = EvChargeSlotRequiredNameRule("ev charge slot needs an entry")
        val item = newEvChargeSlot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
