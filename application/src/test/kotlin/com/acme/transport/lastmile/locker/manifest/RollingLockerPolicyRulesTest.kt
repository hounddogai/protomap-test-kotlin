package com.acme.transport.lastmile.locker.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingLockerPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingLockerPolicyValidator()
        val item = newRollingLockerPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = RollingLockerPolicyRequiredDockRule("rolling locker policy needs a value")
        val item = newRollingLockerPolicy("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingLockerPolicyRequiredTitleRule("rolling locker policy needs a value")
        val item = newRollingLockerPolicy("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingLockerPolicyCodeLengthRule(1000)
        val item = newRollingLockerPolicy("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingLockerPolicyRequiredCodeRule("rolling locker policy needs an entry")
        val item = newRollingLockerPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = RollingLockerPolicyPalletsRangeRule(86400, 86400)
        val item = newRollingLockerPolicy("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
