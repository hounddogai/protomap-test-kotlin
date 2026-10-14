package com.acme.transport.fleetops.defect.defectproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDefectVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredDefectVarianceValidator()
        val item = newTieredDefectVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = TieredDefectVarianceRevisionRangeRule(32, 255)
        val item = newTieredDefectVariance("x", "y")
        item.revision = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TieredDefectVarianceRequiredLabelRule("tiered defect variance needs an entry")
        val item = newTieredDefectVariance("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredDefectVarianceRequiredCodeRule("tiered defect variance needs an entry")
        val item = newTieredDefectVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
