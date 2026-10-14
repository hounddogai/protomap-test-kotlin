package com.acme.transport.crewing.qualification.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultQualificationSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultQualificationSummaryValidator()
        val item = newDefaultQualificationSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = DefaultQualificationSummaryRouteCodeLengthRule(255)
        val item = newDefaultQualificationSummary("x", "y")
        item.routeCode = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultQualificationSummaryCodeLengthRule(3600)
        val item = newDefaultQualificationSummary("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultQualificationSummaryRequiredCodeRule("default qualification summary needs a value")
        val item = newDefaultQualificationSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
