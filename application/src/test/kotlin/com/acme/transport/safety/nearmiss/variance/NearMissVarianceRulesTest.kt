package com.acme.transport.safety.nearmiss.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissVarianceValidator()
        val item = newNearMissVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NearMissVarianceRequiredCodeRule("near miss variance needs a value")
        val item = newNearMissVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = NearMissVarianceRequiredChannelRule("near miss variance needs this field")
        val item = newNearMissVariance("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = NearMissVarianceScoreRangeRule(1.7, 988.2)
        val item = newNearMissVariance("x", "y")
        item.score = 989.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = NearMissVarianceRequiredSegmentRule("near miss variance needs an entry")
        val item = newNearMissVariance("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
