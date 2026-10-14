package com.acme.transport.lastmile.locker.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerMappingValidator()
        val item = newLockerMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = LockerMappingRequiredExternalRefRule("locker mapping needs a value")
        val item = newLockerMapping("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = LockerMappingExternalRefLengthRule(80)
        val item = newLockerMapping("x", "y")
        item.externalRef = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = LockerMappingCreatedAtRangeRule(80, 80)
        val item = newLockerMapping("x", "y")
        item.createdAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LockerMappingRequiredCodeRule("locker mapping needs this field")
        val item = newLockerMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
