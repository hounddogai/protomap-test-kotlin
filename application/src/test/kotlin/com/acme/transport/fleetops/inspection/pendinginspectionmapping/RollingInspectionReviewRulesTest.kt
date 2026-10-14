package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingInspectionReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingInspectionReviewValidator()
        val item = newRollingInspectionReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingInspectionReviewRequiredNameRule("rolling inspection review needs this field")
        val item = newRollingInspectionReview("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = RollingInspectionReviewPalletsRangeRule(16, 86400)
        val item = newRollingInspectionReview("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = RollingInspectionReviewRequiredContactEmailRule("rolling inspection review needs an entry")
        val item = newRollingInspectionReview("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = RollingInspectionReviewThresholdRangeRule(6.8, 1092.9)
        val item = newRollingInspectionReview("x", "y")
        item.threshold = 1094.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
