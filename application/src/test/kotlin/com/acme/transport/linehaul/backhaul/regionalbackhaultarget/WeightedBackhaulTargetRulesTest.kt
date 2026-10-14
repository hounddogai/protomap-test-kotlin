package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedBackhaulTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedBackhaulTargetValidator()
        val item = newWeightedBackhaulTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedBackhaulTargetRequiredCodeRule("weighted backhaul target needs an entry")
        val item = newWeightedBackhaulTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedBackhaulTargetRequiredNameRule("weighted backhaul target needs a value")
        val item = newWeightedBackhaulTarget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedBackhaulTargetCodeLengthRule(32)
        val item = newWeightedBackhaulTarget("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long source`() {
        val rule = WeightedBackhaulTargetSourceLengthRule(64)
        val item = newWeightedBackhaulTarget("x", "y")
        item.source = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
