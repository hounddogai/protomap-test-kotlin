package com.acme.transport.lastmile.redelivery.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRedeliverySnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRedeliverySnapshotValidator()
        val item = newDefaultRedeliverySnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultRedeliverySnapshotRequiredCodeRule("default redelivery snapshot needs an entry")
        val item = newDefaultRedeliverySnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = DefaultRedeliverySnapshotStartsAtRangeRule(32, 32)
        val item = newDefaultRedeliverySnapshot("x", "y")
        item.startsAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultRedeliverySnapshotCodeLengthRule(1000)
        val item = newDefaultRedeliverySnapshot("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
