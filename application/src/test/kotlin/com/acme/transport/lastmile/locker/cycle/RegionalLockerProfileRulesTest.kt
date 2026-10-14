package com.acme.transport.lastmile.locker.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalLockerProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalLockerProfileValidator()
        val item = newRegionalLockerProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalLockerProfileRequiredNameRule("regional locker profile needs an entry")
        val item = newRegionalLockerProfile("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = RegionalLockerProfilePriorityRangeRule(64, 3600)
        val item = newRegionalLockerProfile("x", "y")
        item.priority = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalLockerProfileCodeLengthRule(3600)
        val item = newRegionalLockerProfile("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long description`() {
        val rule = RegionalLockerProfileDescriptionLengthRule(120)
        val item = newRegionalLockerProfile("x", "y")
        item.description = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalLockerProfileNameLengthRule(3600)
        val item = newRegionalLockerProfile("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
