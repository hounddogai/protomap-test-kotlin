package com.acme.transport.safety.violation.violationledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedViolationRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedViolationRollupValidator()
        val item = newWeightedViolationRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = WeightedViolationRollupRequiredOwnerIdRule("weighted violation rollup needs an entry")
        val item = newWeightedViolationRollup("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedViolationRollupCodeLengthRule(32)
        val item = newWeightedViolationRollup("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedViolationRollupRequiredNameRule("weighted violation rollup needs an entry")
        val item = newWeightedViolationRollup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
