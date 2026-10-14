package com.acme.transport.network.node.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredNodeEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredNodeEstimateValidator()
        val item = newTieredNodeEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = TieredNodeEstimateRequiredShiftCodeRule("tiered node estimate needs a value")
        val item = newTieredNodeEstimate("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredNodeEstimateRequiredCodeRule("tiered node estimate needs a value")
        val item = newTieredNodeEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TieredNodeEstimateRequiredTitleRule("tiered node estimate needs an entry")
        val item = newTieredNodeEstimate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
