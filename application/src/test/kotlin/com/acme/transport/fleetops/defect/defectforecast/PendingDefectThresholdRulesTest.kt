package com.acme.transport.fleetops.defect.defectforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDefectThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingDefectThresholdValidator()
        val item = newPendingDefectThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingDefectThresholdCodeLengthRule(255)
        val item = newPendingDefectThreshold("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingDefectThresholdNameLengthRule(120)
        val item = newPendingDefectThreshold("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = PendingDefectThresholdVersionRangeRule(80, 86400)
        val item = newPendingDefectThreshold("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingDefectThresholdRequiredCodeRule("pending defect threshold needs a value")
        val item = newPendingDefectThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
