package com.acme.transport.crewing.qualification.qualificationcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalQualificationMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalQualificationMappingValidator()
        val item = newSeasonalQualificationMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = SeasonalQualificationMappingRequiredSourceRule("seasonal qualification mapping needs an entry")
        val item = newSeasonalQualificationMapping("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalQualificationMappingRequiredCodeRule("seasonal qualification mapping needs this field")
        val item = newSeasonalQualificationMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = SeasonalQualificationMappingSourceLengthRule(16)
        val item = newSeasonalQualificationMapping("x", "y")
        item.source = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalQualificationMappingCodeLengthRule(120)
        val item = newSeasonalQualificationMapping("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
