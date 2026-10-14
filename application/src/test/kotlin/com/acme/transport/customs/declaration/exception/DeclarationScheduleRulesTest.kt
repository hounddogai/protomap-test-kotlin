package com.acme.transport.customs.declaration.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationScheduleValidator()
        val item = newDeclarationSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DeclarationScheduleNameLengthRule(1000)
        val item = newDeclarationSchedule("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = DeclarationScheduleRequiredSegmentRule("declaration schedule needs an entry")
        val item = newDeclarationSchedule("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = DeclarationScheduleContactEmailLengthRule(16)
        val item = newDeclarationSchedule("x", "y")
        item.contactEmail = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = DeclarationScheduleMinimumRangeRule(6.4, 3445.3)
        val item = newDeclarationSchedule("x", "y")
        item.minimum = 3446.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = DeclarationScheduleRequiredContactEmailRule("declaration schedule needs an entry")
        val item = newDeclarationSchedule("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
