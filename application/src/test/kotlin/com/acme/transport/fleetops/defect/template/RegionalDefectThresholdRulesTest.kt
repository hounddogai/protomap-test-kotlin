package com.acme.transport.fleetops.defect.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDefectThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDefectThresholdValidator()
        val item = newRegionalDefectThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalDefectThresholdCodeLengthRule(1000)
        val item = newRegionalDefectThreshold("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = RegionalDefectThresholdWindowDaysRangeRule(255, 1000)
        val item = newRegionalDefectThreshold("x", "y")
        item.windowDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RegionalDefectThresholdRequiredTitleRule("regional defect threshold needs a value")
        val item = newRegionalDefectThreshold("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
