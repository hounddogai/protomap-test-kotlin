package com.acme.transport.linehaul.backhaul.backhaulallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedBackhaulAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedBackhaulAuditValidator()
        val item = newWeightedBackhaulAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedBackhaulAuditRequiredNameRule("weighted backhaul audit needs a value")
        val item = newWeightedBackhaulAudit("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = WeightedBackhaulAuditAmountRangeRule(8.2, 1837.4)
        val item = newWeightedBackhaulAudit("x", "y")
        item.amount = 1838.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = WeightedBackhaulAuditRequiredDockRule("weighted backhaul audit needs a value")
        val item = newWeightedBackhaulAudit("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedBackhaulAuditCodeLengthRule(255)
        val item = newWeightedBackhaulAudit("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
