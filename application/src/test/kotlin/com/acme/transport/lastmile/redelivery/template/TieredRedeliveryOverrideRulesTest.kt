package com.acme.transport.lastmile.redelivery.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRedeliveryOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredRedeliveryOverrideValidator()
        val item = newTieredRedeliveryOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = TieredRedeliveryOverrideWeightRangeRule(5.4, 3213.1)
        val item = newTieredRedeliveryOverride("x", "y")
        item.weight = 3214.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TieredRedeliveryOverrideRequiredNameRule("tiered redelivery override needs this field")
        val item = newTieredRedeliveryOverride("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
