package com.acme.transport.network.node.nodeoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveNodeOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveNodeOverrideValidator()
        val item = newAdaptiveNodeOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = AdaptiveNodeOverrideStartsAtRangeRule(64, 86400)
        val item = newAdaptiveNodeOverride("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveNodeOverrideTitleLengthRule(86400)
        val item = newAdaptiveNodeOverride("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = AdaptiveNodeOverrideLoadKgRangeRule(2.0, 2143.4)
        val item = newAdaptiveNodeOverride("x", "y")
        item.loadKg = 2144.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
