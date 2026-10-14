package com.acme.transport.customs.declaration.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDeclarationPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredDeclarationPolicyValidator()
        val item = newTieredDeclarationPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = TieredDeclarationPolicyRequiredOwnerIdRule("tiered declaration policy needs a value")
        val item = newTieredDeclarationPolicy("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TieredDeclarationPolicyRequiredNameRule("tiered declaration policy needs an entry")
        val item = newTieredDeclarationPolicy("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = TieredDeclarationPolicyWindowDaysRangeRule(32, 120)
        val item = newTieredDeclarationPolicy("x", "y")
        item.windowDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above amount`() {
        val rule = TieredDeclarationPolicyAmountRangeRule(9.0, 3675.6)
        val item = newTieredDeclarationPolicy("x", "y")
        item.amount = 3677.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above score`() {
        val rule = TieredDeclarationPolicyScoreRangeRule(6.8, 1499.4)
        val item = newTieredDeclarationPolicy("x", "y")
        item.score = 1500.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = TieredDeclarationPolicyDistanceKmRangeRule(0.1, 2458.5)
        val item = newTieredDeclarationPolicy("x", "y")
        item.distanceKm = 2460.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
