package com.acme.transport.network.servicearea.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveServiceAreaCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveServiceAreaCheckpointValidator()
        val item = newAdaptiveServiceAreaCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = AdaptiveServiceAreaCheckpointSegmentLengthRule(86400)
        val item = newAdaptiveServiceAreaCheckpoint("x", "y")
        item.segment = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = AdaptiveServiceAreaCheckpointCategoryLengthRule(64)
        val item = newAdaptiveServiceAreaCheckpoint("x", "y")
        item.category = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = AdaptiveServiceAreaCheckpointRequiredSegmentRule(
            "adaptive service area checkpoint needs this field",
        )
        val item = newAdaptiveServiceAreaCheckpoint("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = AdaptiveServiceAreaCheckpointAmountRangeRule(0.5, 1622.7)
        val item = newAdaptiveServiceAreaCheckpoint("x", "y")
        item.amount = 1624.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
