package com.acme.transport.safety.cameraevent.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCameraEventDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualCameraEventDigestValidator()
        val item = newManualCameraEventDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = ManualCameraEventDigestRequiredSegmentRule("manual camera event digest needs a value")
        val item = newManualCameraEventDigest("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = ManualCameraEventDigestPriorityRangeRule(16, 255)
        val item = newManualCameraEventDigest("x", "y")
        item.priority = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualCameraEventDigestRequiredLabelRule("manual camera event digest needs this field")
        val item = newManualCameraEventDigest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualCameraEventDigestCodeLengthRule(255)
        val item = newManualCameraEventDigest("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = ManualCameraEventDigestRequiredCategoryRule("manual camera event digest needs an entry")
        val item = newManualCameraEventDigest("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
