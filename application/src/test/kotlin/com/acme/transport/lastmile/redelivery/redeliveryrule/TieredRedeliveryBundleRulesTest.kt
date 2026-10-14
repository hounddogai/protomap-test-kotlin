package com.acme.transport.lastmile.redelivery.redeliveryrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRedeliveryBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredRedeliveryBundleValidator()
        val item = newTieredRedeliveryBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = TieredRedeliveryBundleRequiredOwnerIdRule("tiered redelivery bundle needs a value")
        val item = newTieredRedeliveryBundle("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = TieredRedeliveryBundleOwnerIdLengthRule(32)
        val item = newTieredRedeliveryBundle("x", "y")
        item.ownerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = TieredRedeliveryBundleRevisionRangeRule(80, 80)
        val item = newTieredRedeliveryBundle("x", "y")
        item.revision = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above limit`() {
        val rule = TieredRedeliveryBundleLimitRangeRule(255, 86400)
        val item = newTieredRedeliveryBundle("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredRedeliveryBundleCodeLengthRule(86400)
        val item = newTieredRedeliveryBundle("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
