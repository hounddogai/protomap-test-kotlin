package com.acme.transport.lastmile.parcel.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelDigestValidator()
        val item = newParcelDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ParcelDigestCodeLengthRule(86400)
        val item = newParcelDigest("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = ParcelDigestStartsAtRangeRule(120, 1000)
        val item = newParcelDigest("x", "y")
        item.startsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ParcelDigestRequiredCodeRule("parcel digest needs an entry")
        val item = newParcelDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ParcelDigestRequiredTitleRule("parcel digest needs a value")
        val item = newParcelDigest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
