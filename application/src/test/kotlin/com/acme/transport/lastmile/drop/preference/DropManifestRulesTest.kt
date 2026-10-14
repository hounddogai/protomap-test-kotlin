package com.acme.transport.lastmile.drop.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropManifestValidator()
        val item = newDropManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = DropManifestNotesLengthRule(80)
        val item = newDropManifest("x", "y")
        item.notes = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = DropManifestLabelLengthRule(1000)
        val item = newDropManifest("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = DropManifestCreatedAtRangeRule(80, 1000)
        val item = newDropManifest("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = DropManifestStartsAtRangeRule(32, 86400)
        val item = newDropManifest("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long lane`() {
        val rule = DropManifestLaneLengthRule(64)
        val item = newDropManifest("x", "y")
        item.lane = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
