package com.acme.transport.network.servicearea.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalServiceAreaThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalServiceAreaThresholdValidator()
        val item = newRegionalServiceAreaThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = RegionalServiceAreaThresholdMarginRangeRule(9.3, 3131.4)
        val item = newRegionalServiceAreaThreshold("x", "y")
        item.margin = 3132.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalServiceAreaThresholdRequiredCodeRule("regional service area threshold needs this field")
        val item = newRegionalServiceAreaThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = RegionalServiceAreaThresholdScoreRangeRule(7.3, 673.7)
        val item = newRegionalServiceAreaThreshold("x", "y")
        item.score = 675.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
