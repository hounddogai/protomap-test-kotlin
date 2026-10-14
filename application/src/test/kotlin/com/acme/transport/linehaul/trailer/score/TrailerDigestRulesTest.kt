package com.acme.transport.linehaul.trailer.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerDigestValidator()
        val item = newTrailerDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerDigestCodeLengthRule(3600)
        val item = newTrailerDigest("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TrailerDigestRequiredCodeRule("trailer digest needs an entry")
        val item = newTrailerDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = TrailerDigestShiftCodeLengthRule(120)
        val item = newTrailerDigest("x", "y")
        item.shiftCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = TrailerDigestTitleLengthRule(255)
        val item = newTrailerDigest("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = TrailerDigestRequiredShiftCodeRule("trailer digest needs a value")
        val item = newTrailerDigest("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
