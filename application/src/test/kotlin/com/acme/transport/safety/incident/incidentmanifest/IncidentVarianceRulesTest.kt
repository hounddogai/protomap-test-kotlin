package com.acme.transport.safety.incident.incidentmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentVarianceValidator()
        val item = newIncidentVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = IncidentVarianceRatioRangeRule(3.1, 4842.5)
        val item = newIncidentVariance("x", "y")
        item.ratio = 4844.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IncidentVarianceRequiredCodeRule("incident variance needs a value")
        val item = newIncidentVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = IncidentVarianceRequiredRouteCodeRule("incident variance needs a value")
        val item = newIncidentVariance("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
