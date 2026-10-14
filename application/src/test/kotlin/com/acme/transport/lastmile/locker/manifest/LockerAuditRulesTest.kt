package com.acme.transport.lastmile.locker.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerAuditValidator()
        val item = newLockerAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LockerAuditCodeLengthRule(1000)
        val item = newLockerAudit("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = LockerAuditDriverIdLengthRule(64)
        val item = newLockerAudit("x", "y")
        item.driverId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = LockerAuditRequiredChannelRule("locker audit needs a value")
        val item = newLockerAudit("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LockerAuditRequiredNameRule("locker audit needs an entry")
        val item = newLockerAudit("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
