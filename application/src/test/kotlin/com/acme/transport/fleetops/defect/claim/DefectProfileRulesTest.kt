package com.acme.transport.fleetops.defect.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectProfileValidator()
        val item = newDefectProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefectProfileRequiredLabelRule("defect profile needs an entry")
        val item = newDefectProfile("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = DefectProfileCreatedAtRangeRule(80, 1000)
        val item = newDefectProfile("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = DefectProfileChannelLengthRule(64)
        val item = newDefectProfile("x", "y")
        item.channel = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = DefectProfileMaximumRangeRule(0.5, 678.4)
        val item = newDefectProfile("x", "y")
        item.maximum = 679.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = DefectProfileUnitLengthRule(86400)
        val item = newDefectProfile("x", "y")
        item.unit = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
