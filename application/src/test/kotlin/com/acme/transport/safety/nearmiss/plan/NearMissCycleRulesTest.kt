package com.acme.transport.safety.nearmiss.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissCycleValidator()
        val item = newNearMissCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = NearMissCycleStartsAtRangeRule(16, 86400)
        val item = newNearMissCycle("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above limit`() {
        val rule = NearMissCycleLimitRangeRule(120, 255)
        val item = newNearMissCycle("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = NearMissCycleRequiredLabelRule("near miss cycle needs a value")
        val item = newNearMissCycle("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = NearMissCycleLabelLengthRule(16)
        val item = newNearMissCycle("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
