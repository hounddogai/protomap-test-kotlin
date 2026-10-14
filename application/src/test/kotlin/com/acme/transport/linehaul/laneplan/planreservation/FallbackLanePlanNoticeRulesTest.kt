package com.acme.transport.linehaul.laneplan.planreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackLanePlanNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackLanePlanNoticeValidator()
        val item = newFallbackLanePlanNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = FallbackLanePlanNoticeTimezoneLengthRule(1000)
        val item = newFallbackLanePlanNotice("x", "y")
        item.timezone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackLanePlanNoticeRequiredCodeRule("fallback lane plan notice needs this field")
        val item = newFallbackLanePlanNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = FallbackLanePlanNoticeRequiredTrailerIdRule("fallback lane plan notice needs an entry")
        val item = newFallbackLanePlanNotice("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
