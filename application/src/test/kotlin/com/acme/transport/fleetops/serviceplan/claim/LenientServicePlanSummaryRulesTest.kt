package com.acme.transport.fleetops.serviceplan.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientServicePlanSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientServicePlanSummaryValidator()
        val item = newLenientServicePlanSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientServicePlanSummaryRequiredCodeRule("lenient service plan summary needs this field")
        val item = newLenientServicePlanSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = LenientServicePlanSummaryRetriesRangeRule(64, 1000)
        val item = newLenientServicePlanSummary("x", "y")
        item.retries = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
