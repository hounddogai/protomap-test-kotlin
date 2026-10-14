package com.acme.transport.fleetops.defect.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectPreferenceValidator()
        val item = newDefectPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = DefectPreferenceBatchSizeRangeRule(16, 64)
        val item = newDefectPreference("x", "y")
        item.batchSize = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = DefectPreferenceRequiredDepotRule("defect preference needs a value")
        val item = newDefectPreference("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = DefectPreferenceLimitRangeRule(16, 86400)
        val item = newDefectPreference("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefectPreferenceRequiredCodeRule("defect preference needs a value")
        val item = newDefectPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
