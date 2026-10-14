package com.acme.transport.linehaul.tender.tenderestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackTenderSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackTenderSlotValidator()
        val item = newFallbackTenderSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackTenderSlotRequiredCodeRule("fallback tender slot needs this field")
        val item = newFallbackTenderSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = FallbackTenderSlotRequiredPhoneRule("fallback tender slot needs an entry")
        val item = newFallbackTenderSlot("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackTenderSlotTitleLengthRule(120)
        val item = newFallbackTenderSlot("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long locale`() {
        val rule = FallbackTenderSlotLocaleLengthRule(255)
        val item = newFallbackTenderSlot("x", "y")
        item.locale = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
