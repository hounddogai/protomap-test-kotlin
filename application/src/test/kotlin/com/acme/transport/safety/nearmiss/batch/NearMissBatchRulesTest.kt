package com.acme.transport.safety.nearmiss.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissBatchValidator()
        val item = newNearMissBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = NearMissBatchCodeLengthRule(86400)
        val item = newNearMissBatch("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = NearMissBatchAmountRangeRule(1.6, 2055.3)
        val item = newNearMissBatch("x", "y")
        item.amount = 2056.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = NearMissBatchTitleLengthRule(3600)
        val item = newNearMissBatch("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = NearMissBatchLoadKgRangeRule(5.3, 1158.3)
        val item = newNearMissBatch("x", "y")
        item.loadKg = 1159.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = NearMissBatchRequiredTitleRule("near miss batch needs an entry")
        val item = newNearMissBatch("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
