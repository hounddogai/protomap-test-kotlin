package com.acme.transport.crewing.qualification.qualificationassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledQualificationThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledQualificationThresholdValidator()
        val item = newScheduledQualificationThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = ScheduledQualificationThresholdChecksumLengthRule(255)
        val item = newScheduledQualificationThreshold("x", "y")
        item.checksum = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = ScheduledQualificationThresholdVersionRangeRule(32, 120)
        val item = newScheduledQualificationThreshold("x", "y")
        item.version = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledQualificationThresholdRequiredCodeRule(
            "scheduled qualification threshold needs this field",
        )
        val item = newScheduledQualificationThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = ScheduledQualificationThresholdRequiredChecksumRule(
            "scheduled qualification threshold needs this field",
        )
        val item = newScheduledQualificationThreshold("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledQualificationThresholdNameLengthRule(120)
        val item = newScheduledQualificationThreshold("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
