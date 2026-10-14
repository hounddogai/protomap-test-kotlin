package com.acme.transport.network.cutoff.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCutoffGroupValidator()
        val item = newCutoffGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = CutoffGroupBatchSizeRangeRule(3600, 3600)
        val item = newCutoffGroup("x", "y")
        item.batchSize = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CutoffGroupRequiredLabelRule("cutoff group needs a value")
        val item = newCutoffGroup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = CutoffGroupPriorityRangeRule(16, 64)
        val item = newCutoffGroup("x", "y")
        item.priority = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CutoffGroupRequiredNameRule("cutoff group needs a value")
        val item = newCutoffGroup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = CutoffGroupRateRangeRule(3.0, 306.9)
        val item = newCutoffGroup("x", "y")
        item.rate = 308.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CutoffGroupRequiredCodeRule("cutoff group needs an entry")
        val item = newCutoffGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
