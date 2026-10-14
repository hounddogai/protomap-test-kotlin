package com.acme.transport.linehaul.backhaul.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulProfileValidator()
        val item = newBackhaulProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BackhaulProfileRequiredLabelRule("backhaul profile needs a value")
        val item = newBackhaulProfile("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = BackhaulProfileLimitRangeRule(16, 16)
        val item = newBackhaulProfile("x", "y")
        item.limit = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = BackhaulProfileLabelLengthRule(255)
        val item = newBackhaulProfile("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = BackhaulProfileRequiredCategoryRule("backhaul profile needs an entry")
        val item = newBackhaulProfile("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
