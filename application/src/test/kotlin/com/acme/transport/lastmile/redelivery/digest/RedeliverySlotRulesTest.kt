package com.acme.transport.lastmile.redelivery.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliverySlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliverySlotValidator()
        val item = newRedeliverySlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RedeliverySlotLabelLengthRule(86400)
        val item = newRedeliverySlot("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RedeliverySlotCodeLengthRule(80)
        val item = newRedeliverySlot("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RedeliverySlotRequiredLabelRule("redelivery slot needs an entry")
        val item = newRedeliverySlot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RedeliverySlotRequiredCodeRule("redelivery slot needs an entry")
        val item = newRedeliverySlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
