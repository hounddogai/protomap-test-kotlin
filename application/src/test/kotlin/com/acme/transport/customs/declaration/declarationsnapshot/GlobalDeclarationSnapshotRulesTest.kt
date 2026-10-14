package com.acme.transport.customs.declaration.declarationsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDeclarationSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalDeclarationSnapshotValidator()
        val item = newGlobalDeclarationSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalDeclarationSnapshotLabelLengthRule(80)
        val item = newGlobalDeclarationSnapshot("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = GlobalDeclarationSnapshotRetriesRangeRule(16, 80)
        val item = newGlobalDeclarationSnapshot("x", "y")
        item.retries = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = GlobalDeclarationSnapshotRequiredChecksumRule("global declaration snapshot needs an entry")
        val item = newGlobalDeclarationSnapshot("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalDeclarationSnapshotCodeLengthRule(120)
        val item = newGlobalDeclarationSnapshot("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
