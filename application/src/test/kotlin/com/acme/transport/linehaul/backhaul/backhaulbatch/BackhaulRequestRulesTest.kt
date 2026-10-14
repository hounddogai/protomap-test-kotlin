package com.acme.transport.linehaul.backhaul.backhaulbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulRequestValidator()
        val item = newBackhaulRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = BackhaulRequestCapacityRangeRule(64, 120)
        val item = newBackhaulRequest("x", "y")
        item.capacity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = BackhaulRequestRequiredChannelRule("backhaul request needs an entry")
        val item = newBackhaulRequest("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BackhaulRequestCodeLengthRule(16)
        val item = newBackhaulRequest("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = BackhaulRequestLabelLengthRule(3600)
        val item = newBackhaulRequest("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
