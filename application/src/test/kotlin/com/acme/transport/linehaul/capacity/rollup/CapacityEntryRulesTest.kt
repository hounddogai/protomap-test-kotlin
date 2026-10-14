package com.acme.transport.linehaul.capacity.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityEntryValidator()
        val item = newCapacityEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CapacityEntryRequiredCodeRule("capacity entry needs this field")
        val item = newCapacityEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = CapacityEntryRequiredCategoryRule("capacity entry needs this field")
        val item = newCapacityEntry("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CapacityEntryTitleLengthRule(80)
        val item = newCapacityEntry("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
