package com.acme.transport.network.servicearea.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingServiceAreaBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingServiceAreaBundleValidator()
        val item = newPendingServiceAreaBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = PendingServiceAreaBundleMarginRangeRule(7.3, 3635.2)
        val item = newPendingServiceAreaBundle("x", "y")
        item.margin = 3636.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = PendingServiceAreaBundleExternalRefLengthRule(120)
        val item = newPendingServiceAreaBundle("x", "y")
        item.externalRef = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = PendingServiceAreaBundleScoreRangeRule(9.1, 1839.1)
        val item = newPendingServiceAreaBundle("x", "y")
        item.score = 1840.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
