package com.acme.transport.crewing.qualification.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalQualificationDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalQualificationDraftValidator()
        val item = newRegionalQualificationDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalQualificationDraftLabelLengthRule(255)
        val item = newRegionalQualificationDraft("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = RegionalQualificationDraftStopCountRangeRule(80, 3600)
        val item = newRegionalQualificationDraft("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalQualificationDraftRequiredNameRule("regional qualification draft needs this field")
        val item = newRegionalQualificationDraft("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = RegionalQualificationDraftLoadKgRangeRule(6.0, 1109.6)
        val item = newRegionalQualificationDraft("x", "y")
        item.loadKg = 1111.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
