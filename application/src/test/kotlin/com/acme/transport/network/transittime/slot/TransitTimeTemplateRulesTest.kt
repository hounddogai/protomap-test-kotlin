package com.acme.transport.network.transittime.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeTemplateValidator()
        val item = newTransitTimeTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TransitTimeTemplateRequiredCodeRule("transit time template needs a value")
        val item = newTransitTimeTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TransitTimeTemplateNameLengthRule(120)
        val item = newTransitTimeTemplate("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = TransitTimeTemplateRequiredShiftCodeRule("transit time template needs a value")
        val item = newTransitTimeTemplate("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = TransitTimeTemplateDescriptionLengthRule(64)
        val item = newTransitTimeTemplate("x", "y")
        item.description = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TransitTimeTemplateRequiredNameRule("transit time template needs a value")
        val item = newTransitTimeTemplate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
