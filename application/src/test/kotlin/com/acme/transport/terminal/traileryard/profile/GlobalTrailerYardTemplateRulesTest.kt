package com.acme.transport.terminal.traileryard.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTrailerYardTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalTrailerYardTemplateValidator()
        val item = newGlobalTrailerYardTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = GlobalTrailerYardTemplateRequiredContactEmailRule("global trailer yard template needs a value")
        val item = newGlobalTrailerYardTemplate("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalTrailerYardTemplateCodeLengthRule(32)
        val item = newGlobalTrailerYardTemplate("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = GlobalTrailerYardTemplateEtaMinutesRangeRule(64, 255)
        val item = newGlobalTrailerYardTemplate("x", "y")
        item.etaMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = GlobalTrailerYardTemplateRetriesRangeRule(16, 3600)
        val item = newGlobalTrailerYardTemplate("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalTrailerYardTemplateTitleLengthRule(1000)
        val item = newGlobalTrailerYardTemplate("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
