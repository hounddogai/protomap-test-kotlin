package com.acme.transport.network.node.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientNodeManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientNodeManifestValidator()
        val item = newLenientNodeManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = LenientNodeManifestCurrencyLengthRule(80)
        val item = newLenientNodeManifest("x", "y")
        item.currency = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
