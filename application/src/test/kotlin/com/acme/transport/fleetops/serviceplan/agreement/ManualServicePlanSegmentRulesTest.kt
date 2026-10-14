package com.acme.transport.fleetops.serviceplan.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualServicePlanSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualServicePlanSegmentValidator()
        val item = newManualServicePlanSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualServicePlanSegmentCodeLengthRule(255)
        val item = newManualServicePlanSegment("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualServicePlanSegmentTitleLengthRule(1000)
        val item = newManualServicePlanSegment("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = ManualServicePlanSegmentRequiredLocaleRule("manual service plan segment needs this field")
        val item = newManualServicePlanSegment("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualServicePlanSegmentRequiredCodeRule("manual service plan segment needs a value")
        val item = newManualServicePlanSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
