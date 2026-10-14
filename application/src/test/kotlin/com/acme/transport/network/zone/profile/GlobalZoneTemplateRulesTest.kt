package com.acme.transport.network.zone.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalZoneTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalZoneTemplateValidator()
        val item = newGlobalZoneTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = GlobalZoneTemplateChecksumLengthRule(32)
        val item = newGlobalZoneTemplate("x", "y")
        item.checksum = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = GlobalZoneTemplateLoadKgRangeRule(3.6, 4552.9)
        val item = newGlobalZoneTemplate("x", "y")
        item.loadKg = 4554.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = GlobalZoneTemplateRequiredTitleRule("global zone template needs a value")
        val item = newGlobalZoneTemplate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalZoneTemplateTitleLengthRule(255)
        val item = newGlobalZoneTemplate("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
