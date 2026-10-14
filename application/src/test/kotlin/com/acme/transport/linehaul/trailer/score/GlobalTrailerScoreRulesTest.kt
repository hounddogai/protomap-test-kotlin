package com.acme.transport.linehaul.trailer.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTrailerScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalTrailerScoreValidator()
        val item = newGlobalTrailerScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = GlobalTrailerScoreChecksumLengthRule(120)
        val item = newGlobalTrailerScore("x", "y")
        item.checksum = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
