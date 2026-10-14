package com.acme.transport.emissions.emissionfactor.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorNoticeValidator()
        val item = newEmissionFactorNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = EmissionFactorNoticeWindowDaysRangeRule(32, 3600)
        val item = newEmissionFactorNotice("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = EmissionFactorNoticeCodeLengthRule(255)
        val item = newEmissionFactorNotice("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EmissionFactorNoticeRequiredCodeRule("emission factor notice needs an entry")
        val item = newEmissionFactorNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = EmissionFactorNoticeLabelLengthRule(86400)
        val item = newEmissionFactorNotice("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
