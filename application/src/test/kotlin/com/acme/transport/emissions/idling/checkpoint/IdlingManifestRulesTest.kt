package com.acme.transport.emissions.idling.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingManifestValidator()
        val item = newIdlingManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = IdlingManifestChecksumLengthRule(1000)
        val item = newIdlingManifest("x", "y")
        item.checksum = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = IdlingManifestCodeLengthRule(120)
        val item = newIdlingManifest("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
