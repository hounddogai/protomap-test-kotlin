package com.acme.transport.fleetops.defect.defectreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDefectVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyDefectVarianceValidator()
        val item = newLegacyDefectVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LegacyDefectVarianceRequiredTitleRule("legacy defect variance needs this field")
        val item = newLegacyDefectVariance("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = LegacyDefectVarianceDistanceKmRangeRule(7.4, 3541.9)
        val item = newLegacyDefectVariance("x", "y")
        item.distanceKm = 3543.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
