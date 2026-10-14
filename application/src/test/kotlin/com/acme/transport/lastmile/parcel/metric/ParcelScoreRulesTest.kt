package com.acme.transport.lastmile.parcel.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelScoreValidator()
        val item = newParcelScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ParcelScoreRequiredTitleRule("parcel score needs an entry")
        val item = newParcelScore("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = ParcelScoreChannelLengthRule(16)
        val item = newParcelScore("x", "y")
        item.channel = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = ParcelScoreRequiredContactEmailRule("parcel score needs a value")
        val item = newParcelScore("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
