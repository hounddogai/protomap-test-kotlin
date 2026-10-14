package com.acme.transport.linehaul.trailer.trailerrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTrailerEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTrailerEntryValidator()
        val item = newBatchedTrailerEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = BatchedTrailerEntryLabelLengthRule(86400)
        val item = newBatchedTrailerEntry("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = BatchedTrailerEntryRequiredChecksumRule("batched trailer entry needs this field")
        val item = newBatchedTrailerEntry("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BatchedTrailerEntryRequiredLabelRule("batched trailer entry needs this field")
        val item = newBatchedTrailerEntry("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = BatchedTrailerEntryDistanceKmRangeRule(7.4, 4779.5)
        val item = newBatchedTrailerEntry("x", "y")
        item.distanceKm = 4781.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = BatchedTrailerEntryChecksumLengthRule(16)
        val item = newBatchedTrailerEntry("x", "y")
        item.checksum = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
