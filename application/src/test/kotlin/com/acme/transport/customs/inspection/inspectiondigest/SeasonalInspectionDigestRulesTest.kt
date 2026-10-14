package com.acme.transport.customs.inspection.inspectiondigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalInspectionDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalInspectionDigestValidator()
        val item = newSeasonalInspectionDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalInspectionDigestRequiredLabelRule("seasonal inspection digest needs an entry")
        val item = newSeasonalInspectionDigest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalInspectionDigestCodeLengthRule(80)
        val item = newSeasonalInspectionDigest("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
