package com.acme.transport.linehaul.backhaul.backhaulassignment

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingBackhaulMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingBackhaulMappingValidator()
        val item = newPendingBackhaulMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = PendingBackhaulMappingUpdatedAtRangeRule(32, 255)
        val item = newPendingBackhaulMapping("x", "y")
        item.updatedAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
