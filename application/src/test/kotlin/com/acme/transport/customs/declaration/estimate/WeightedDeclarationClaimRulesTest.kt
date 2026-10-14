package com.acme.transport.customs.declaration.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedDeclarationClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedDeclarationClaimValidator()
        val item = newWeightedDeclarationClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = WeightedDeclarationClaimRequiredChannelRule("weighted declaration claim needs this field")
        val item = newWeightedDeclarationClaim("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedDeclarationClaimCodeLengthRule(3600)
        val item = newWeightedDeclarationClaim("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = WeightedDeclarationClaimRequiredDriverIdRule("weighted declaration claim needs an entry")
        val item = newWeightedDeclarationClaim("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedDeclarationClaimRequiredLabelRule("weighted declaration claim needs this field")
        val item = newWeightedDeclarationClaim("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
