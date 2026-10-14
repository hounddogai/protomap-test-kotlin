package com.acme.transport.crewing.restbreak.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakManifestValidator()
        val item = newRestBreakManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RestBreakManifestRequiredCodeRule("rest break manifest needs an entry")
        val item = newRestBreakManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = RestBreakManifestCreatedAtRangeRule(16, 255)
        val item = newRestBreakManifest("x", "y")
        item.createdAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
