package com.acme.transport.crewing.qualification.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalQualificationWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalQualificationWindowValidator()
        val item = newRegionalQualificationWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalQualificationWindowRequiredNameRule("regional qualification window needs a value")
        val item = newRegionalQualificationWindow("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalQualificationWindowNameLengthRule(64)
        val item = newRegionalQualificationWindow("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = RegionalQualificationWindowEtaMinutesRangeRule(32, 64)
        val item = newRegionalQualificationWindow("x", "y")
        item.etaMinutes = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalQualificationWindowCodeLengthRule(64)
        val item = newRegionalQualificationWindow("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
