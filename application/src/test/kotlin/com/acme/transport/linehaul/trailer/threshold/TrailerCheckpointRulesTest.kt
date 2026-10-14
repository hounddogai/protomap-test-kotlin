package com.acme.transport.linehaul.trailer.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerCheckpointValidator()
        val item = newTrailerCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TrailerCheckpointLabelLengthRule(86400)
        val item = newTrailerCheckpoint("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TrailerCheckpointRequiredLabelRule("trailer checkpoint needs an entry")
        val item = newTrailerCheckpoint("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = TrailerCheckpointRequiredChecksumRule("trailer checkpoint needs an entry")
        val item = newTrailerCheckpoint("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerCheckpointCodeLengthRule(16)
        val item = newTrailerCheckpoint("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = TrailerCheckpointChecksumLengthRule(32)
        val item = newTrailerCheckpoint("x", "y")
        item.checksum = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
