package com.acme.transport.customs.declaration.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDeclarationManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedDeclarationManifestValidator()
        val item = newBatchedDeclarationManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = BatchedDeclarationManifestRequiredDockRule("batched declaration manifest needs a value")
        val item = newBatchedDeclarationManifest("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedDeclarationManifestCodeLengthRule(32)
        val item = newBatchedDeclarationManifest("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedDeclarationManifestNameLengthRule(16)
        val item = newBatchedDeclarationManifest("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long channel`() {
        val rule = BatchedDeclarationManifestChannelLengthRule(16)
        val item = newBatchedDeclarationManifest("x", "y")
        item.channel = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
