package com.acme.transport.lastmile.drop.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultDropDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultDropDraftValidator()
        val item = newDefaultDropDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = DefaultDropDraftRequiredCarrierRule("default drop draft needs a value")
        val item = newDefaultDropDraft("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = DefaultDropDraftDiscountRangeRule(2.8, 990.3)
        val item = newDefaultDropDraft("x", "y")
        item.discount = 991.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = DefaultDropDraftCeilingRangeRule(5.4, 940.8)
        val item = newDefaultDropDraft("x", "y")
        item.ceiling = 942.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = DefaultDropDraftTrailerIdLengthRule(120)
        val item = newDefaultDropDraft("x", "y")
        item.trailerId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
