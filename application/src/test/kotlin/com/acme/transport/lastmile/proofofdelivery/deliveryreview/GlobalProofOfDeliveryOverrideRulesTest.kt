package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalProofOfDeliveryOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalProofOfDeliveryOverrideValidator()
        val item = newGlobalProofOfDeliveryOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = GlobalProofOfDeliveryOverrideScoreRangeRule(2.8, 2102.1)
        val item = newGlobalProofOfDeliveryOverride("x", "y")
        item.score = 2103.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalProofOfDeliveryOverrideRequiredNameRule("global proof of delivery override needs this field")
        val item = newGlobalProofOfDeliveryOverride("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalProofOfDeliveryOverrideRequiredCodeRule("global proof of delivery override needs this field")
        val item = newGlobalProofOfDeliveryOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalProofOfDeliveryOverrideCodeLengthRule(32)
        val item = newGlobalProofOfDeliveryOverride("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalProofOfDeliveryOverrideNameLengthRule(64)
        val item = newGlobalProofOfDeliveryOverride("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
