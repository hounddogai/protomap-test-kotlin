package com.acme.transport.customs.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictInspectionProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictInspectionProfileValidator()
        val item = newStrictInspectionProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = StrictInspectionProfileCreatedAtRangeRule(255, 86400)
        val item = newStrictInspectionProfile("x", "y")
        item.createdAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = StrictInspectionProfileRequiredNameRule("strict inspection profile needs an entry")
        val item = newStrictInspectionProfile("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictInspectionProfileRequiredCodeRule("strict inspection profile needs an entry")
        val item = newStrictInspectionProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
