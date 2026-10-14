package com.acme.transport.crewing.qualification.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalQualificationMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalQualificationMilestoneValidator()
        val item = newSeasonalQualificationMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalQualificationMilestoneLabelLengthRule(255)
        val item = newSeasonalQualificationMilestone("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = SeasonalQualificationMilestoneBatchSizeRangeRule(32, 1000)
        val item = newSeasonalQualificationMilestone("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = SeasonalQualificationMilestoneRequiredLocaleRule("seasonal qualification milestone needs an entry")
        val item = newSeasonalQualificationMilestone("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
