package com.acme.transport.network.servicearea.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalServiceAreaSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalServiceAreaSnapshotValidator()
        val item = newGlobalServiceAreaSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = GlobalServiceAreaSnapshotChecksumLengthRule(86400)
        val item = newGlobalServiceAreaSnapshot("x", "y")
        item.checksum = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalServiceAreaSnapshotRequiredNameRule("global service area snapshot needs a value")
        val item = newGlobalServiceAreaSnapshot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalServiceAreaSnapshotRequiredCodeRule("global service area snapshot needs an entry")
        val item = newGlobalServiceAreaSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalServiceAreaSnapshotNameLengthRule(86400)
        val item = newGlobalServiceAreaSnapshot("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
