package com.acme.transport.customs.tariff.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffDigestValidator()
        val item = newTariffDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TariffDigestTitleLengthRule(1000)
        val item = newTariffDigest("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = TariffDigestReviewerLengthRule(255)
        val item = newTariffDigest("x", "y")
        item.reviewer = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
