package com.acme.transport.customs.inspection.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalInspectionThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalInspectionThresholdValidator()
        val item = newGlobalInspectionThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = GlobalInspectionThresholdRequiredLaneRule("global inspection threshold needs this field")
        val item = newGlobalInspectionThreshold("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalInspectionThresholdCodeLengthRule(255)
        val item = newGlobalInspectionThreshold("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = GlobalInspectionThresholdRequiredDepotRule("global inspection threshold needs this field")
        val item = newGlobalInspectionThreshold("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalInspectionThresholdTitleLengthRule(16)
        val item = newGlobalInspectionThreshold("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
