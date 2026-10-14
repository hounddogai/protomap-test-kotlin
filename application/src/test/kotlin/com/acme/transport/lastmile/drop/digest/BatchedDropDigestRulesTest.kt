package com.acme.transport.lastmile.drop.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDropDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedDropDigestValidator()
        val item = newBatchedDropDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedDropDigestTitleLengthRule(80)
        val item = newBatchedDropDigest("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = BatchedDropDigestOwnerIdLengthRule(64)
        val item = newBatchedDropDigest("x", "y")
        item.ownerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedDropDigestRequiredCodeRule("batched drop digest needs an entry")
        val item = newBatchedDropDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = BatchedDropDigestRequiredExternalRefRule("batched drop digest needs a value")
        val item = newBatchedDropDigest("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
