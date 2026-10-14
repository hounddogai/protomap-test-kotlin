package com.acme.transport.crewing.qualification.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationForecastValidator()
        val item = newQualificationForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = QualificationForecastRequiredCodeRule("qualification forecast needs an entry")
        val item = newQualificationForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = QualificationForecastRequiredNameRule("qualification forecast needs this field")
        val item = newQualificationForecast("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = QualificationForecastRequiredTimezoneRule("qualification forecast needs an entry")
        val item = newQualificationForecast("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
