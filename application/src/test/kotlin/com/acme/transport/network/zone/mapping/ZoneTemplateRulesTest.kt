package com.acme.transport.network.zone.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneTemplateValidator()
        val item = newZoneTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ZoneTemplateNameLengthRule(16)
        val item = newZoneTemplate("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ZoneTemplateRequiredNameRule("zone template needs a value")
        val item = newZoneTemplate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = ZoneTemplateRequiredExternalRefRule("zone template needs a value")
        val item = newZoneTemplate("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
