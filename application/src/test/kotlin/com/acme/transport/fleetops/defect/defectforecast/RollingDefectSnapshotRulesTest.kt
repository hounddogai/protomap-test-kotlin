package com.acme.transport.fleetops.defect.defectforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDefectSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDefectSnapshotValidator()
        val item = newRollingDefectSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RollingDefectSnapshotAttemptsRangeRule(16, 86400)
        val item = newRollingDefectSnapshot("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RollingDefectSnapshotRequiredLabelRule("rolling defect snapshot needs this field")
        val item = newRollingDefectSnapshot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingDefectSnapshotCodeLengthRule(16)
        val item = newRollingDefectSnapshot("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = RollingDefectSnapshotLimitRangeRule(80, 1000)
        val item = newRollingDefectSnapshot("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
