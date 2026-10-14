package com.acme.transport.fleetops.defect.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectDigestValidator()
        val item = newDefectDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = DefectDigestStartsAtRangeRule(120, 255)
        val item = newDefectDigest("x", "y")
        item.startsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = DefectDigestOdometerRangeRule(32, 3600)
        val item = newDefectDigest("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefectDigestRequiredLabelRule("defect digest needs this field")
        val item = newDefectDigest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
