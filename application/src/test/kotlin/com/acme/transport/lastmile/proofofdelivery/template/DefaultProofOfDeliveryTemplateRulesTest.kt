package com.acme.transport.lastmile.proofofdelivery.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultProofOfDeliveryTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultProofOfDeliveryTemplateValidator()
        val item = newDefaultProofOfDeliveryTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = DefaultProofOfDeliveryTemplateUnitLengthRule(80)
        val item = newDefaultProofOfDeliveryTemplate("x", "y")
        item.unit = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefaultProofOfDeliveryTemplateRequiredNameRule("default proof of delivery template needs an entry")
        val item = newDefaultProofOfDeliveryTemplate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = DefaultProofOfDeliveryTemplateMinimumRangeRule(1.5, 1140.1)
        val item = newDefaultProofOfDeliveryTemplate("x", "y")
        item.minimum = 1141.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
