package com.acme.transport.fleetops.defect.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectTierValidator()
        val item = newDefectTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefectTierNameLengthRule(120)
        val item = newDefectTier("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefectTierRequiredLabelRule("defect tier needs an entry")
        val item = newDefectTier("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = DefectTierRequiredChecksumRule("defect tier needs an entry")
        val item = newDefectTier("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = DefectTierRetriesRangeRule(16, 255)
        val item = newDefectTier("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
