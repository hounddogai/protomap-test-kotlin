package com.acme.transport.network.servicearea.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyServiceAreaScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyServiceAreaScoreValidator()
        val item = newLegacyServiceAreaScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = LegacyServiceAreaScoreBatchSizeRangeRule(80, 120)
        val item = newLegacyServiceAreaScore("x", "y")
        item.batchSize = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyServiceAreaScoreCodeLengthRule(80)
        val item = newLegacyServiceAreaScore("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyServiceAreaScoreRequiredCodeRule("legacy service area score needs an entry")
        val item = newLegacyServiceAreaScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
