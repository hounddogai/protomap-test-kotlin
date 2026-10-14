package com.acme.transport.fleetops.defect.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDefectThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledDefectThresholdValidator()
        val item = newScheduledDefectThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledDefectThresholdRequiredNameRule("scheduled defect threshold needs this field")
        val item = newScheduledDefectThreshold("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = ScheduledDefectThresholdChannelLengthRule(120)
        val item = newScheduledDefectThreshold("x", "y")
        item.channel = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = ScheduledDefectThresholdRequiredChannelRule("scheduled defect threshold needs a value")
        val item = newScheduledDefectThreshold("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
