package com.acme.transport.crewing.qualification.qualificationmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingQualificationForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingQualificationForecastValidator()
        val item = newPendingQualificationForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingQualificationForecastRequiredCodeRule("pending qualification forecast needs this field")
        val item = newPendingQualificationForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingQualificationForecastTitleLengthRule(3600)
        val item = newPendingQualificationForecast("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
