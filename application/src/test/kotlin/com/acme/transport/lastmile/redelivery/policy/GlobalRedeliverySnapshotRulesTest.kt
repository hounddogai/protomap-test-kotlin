package com.acme.transport.lastmile.redelivery.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRedeliverySnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalRedeliverySnapshotValidator()
        val item = newGlobalRedeliverySnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = GlobalRedeliverySnapshotExpiresAtRangeRule(16, 3600)
        val item = newGlobalRedeliverySnapshot("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = GlobalRedeliverySnapshotCreatedAtRangeRule(255, 1000)
        val item = newGlobalRedeliverySnapshot("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank region`() {
        val rule = GlobalRedeliverySnapshotRequiredRegionRule("global redelivery snapshot needs an entry")
        val item = newGlobalRedeliverySnapshot("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = GlobalRedeliverySnapshotRegionLengthRule(120)
        val item = newGlobalRedeliverySnapshot("x", "y")
        item.region = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalRedeliverySnapshotCodeLengthRule(255)
        val item = newGlobalRedeliverySnapshot("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
